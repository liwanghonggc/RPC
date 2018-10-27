package com.lwh.rpc.provider;

import com.google.common.collect.Maps;
import com.lwh.rpc.model.ProviderService;
import com.lwh.rpc.model.RpcRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 1)Netty服务端接收客户端发起的请求字节数组,然后根据NettyDecoderHandler将字节数组解码为对应的Java请求对象.
 *       2)然后根据解码得到的Java请求对象确定服务提供者的接口及方法,根据Java反射调用
 */

@ChannelHandler.Sharable
public class NettyServerInvokerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerInvokerHandler.class);

    /**
     * 服务端限流
     */
    private static final Map<String, Semaphore> serviceKeySemaphoreMap = Maps.newConcurrentMap();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发生异常,关闭链路
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        if(ctx.channel().isWritable()){
            //从服务调用对象里获取服务提供者信息
            ProviderService metaDataModel = rpcRequest.getProviderService();
            long consumeTimeout = rpcRequest.getInvokeTimeout();
            final String methodName = rpcRequest.getInvokeMethodName();

            //根据方法名称定位到具体某一个服务提供者
            String serviceKey = metaDataModel.getServiceItf().getName();
            //获取限流工具类
            int workerThread = metaDataModel.getWorkerThreads();
            Semaphore semaphore = serviceKeySemaphoreMap.get(serviceKey);

            //初始化流控基础设施semaphore
            if(semaphore == null){
                synchronized (serviceKeySemaphoreMap){
                    semaphore = serviceKeySemaphoreMap.get(serviceKey);
                    if(semaphore == null){
                        semaphore = new Semaphore(workerThread);
                        serviceKeySemaphoreMap.put(serviceKey, semaphore);
                    }
                }
            }
        }
    }
}
