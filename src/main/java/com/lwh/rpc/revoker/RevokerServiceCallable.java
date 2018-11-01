package com.lwh.rpc.revoker;

import com.lwh.rpc.model.RpcRequest;
import com.lwh.rpc.model.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp Netty客户端如何发起服务调用
 */
public class RevokerServiceCallable implements Callable<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RevokerServiceCallable.class);

    private Channel channel;

    private InetSocketAddress inetSocketAddress;

    private RpcRequest request;

    public static RevokerServiceCallable of(InetSocketAddress inetSocketAddress, RpcRequest request){
        return new RevokerServiceCallable(inetSocketAddress, request);
    }

    public RevokerServiceCallable(InetSocketAddress inetSocketAddress, RpcRequest request){
        this.inetSocketAddress = inetSocketAddress;
        this.request = request;
    }

    @Override
    public RpcResponse call() throws Exception {
        //初始化结果返回容器,将本次调用的唯一标识作为Key存入返回结果的Map
        RevokerResponseHolder.initResponseData(request.getUniquekey());
        //根据本地调用服务提供者地址获取对应的Netty的通道Channel队列
        NettyChannelPoolFactory channelPoolFactory = NettyChannelPoolFactory.getInstance();
        ArrayBlockingQueue<Channel> blockingQueue = channelPoolFactory.acquire(inetSocketAddress);
        try {
            if(channel == null){
                //从队列通道中获取本次调用的Netty通道channel
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            }

            //若获取的channel通道已经不可用,则重新获取一个
            while( !channel.isOpen() || !channel.isActive() || !channel.isWritable()){
                logger.warn("-----------retry get new channel-------------");
                System.out.println("-----------retry get new channel-------------");
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
                if(channel == null){
                    //若队列中没有可用的Channel,则重新注册一个
                    channel = channelPoolFactory.registerChannel(inetSocketAddress);
                }
            }

            //将本次调用信息写入Netty通道,发起异步调用
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();

            //从返回结果容器中获取返回结果,同时设置等待超时时间为invokeTimeout
            long invokeTimeout = request.getInvokeTimeout();
            return RevokerResponseHolder.getValue(request.getUniquekey(), invokeTimeout);
        } catch (Exception e) {
            logger.warn("service invoker error", e);
            System.out.println("service invoker error, e = " + e.getMessage());
        } finally {
            //本次调用完毕之后,将Netty的通道channel重新释放到队列中,以便下次重复调用
            channelPoolFactory.release(blockingQueue, channel, inetSocketAddress);
        }
        return null;
    }




































}
