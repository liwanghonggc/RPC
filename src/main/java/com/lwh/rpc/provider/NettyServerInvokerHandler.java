package com.lwh.rpc.provider;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.lwh.rpc.model.ProviderService;
import com.lwh.rpc.model.RpcRequest;
import com.lwh.rpc.model.RpcResponse;
import com.lwh.rpc.zookeeper.IRegisterCenter4Provider;
import com.lwh.rpc.zookeeper.RegisterCenter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

            //获取注册中心服务
            IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
            List<ProviderService> localProvidersCaches = registerCenter4Provider.getProviderServiceMap().get(serviceKey);

            Object result = null;
            boolean acquire = false;

            try{
                ProviderService localProviderCache = Collections2.filter(localProvidersCaches, new Predicate<ProviderService>() {
                    @Override
                    public boolean apply(ProviderService input) {
                        return StringUtils.equals(input.getServiceMethod().getName(), methodName);
                    }
                }).iterator().next();

                Object serviceObject = localProviderCache.getServiceObject();
                //利用反射发起服务调用
                Method method = localProviderCache.getServiceMethod();
                //利用semaphore实现限流
                acquire = semaphore.tryAcquire(consumeTimeout, TimeUnit.MICROSECONDS);
                if(acquire){
                    result = method.invoke(serviceObject, rpcRequest.getArgs());
                    System.out.println("调用结果为: " + result);
                }
            }catch (Exception e){
                System.out.println(JSON.toJSONString(localProvidersCaches) + "  " + methodName+" "+e.getMessage());
                result = e;
            }finally {
                if (acquire){
                    semaphore.release();
                }
            }

            //根据服务调用结果封装调用返回对象
            RpcResponse response = new RpcResponse();
            response.setInvokeTimeout(consumeTimeout);
            response.setUniqueKey(rpcRequest.getUniquekey());
            response.setResult(result);

            //将服务调用结果返回对象写回到消费端
            ctx.writeAndFlush(response);
        }else {
            logger.error("channel closed!");
            System.out.println("channel closed!");
        }
    }
}
