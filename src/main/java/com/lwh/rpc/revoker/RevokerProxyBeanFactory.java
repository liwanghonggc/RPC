package com.lwh.rpc.revoker;

import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.cluster.engine.ClusterEngine;
import com.lwh.rpc.model.ProviderService;
import com.lwh.rpc.model.RpcRequest;
import com.lwh.rpc.model.RpcResponse;
import com.lwh.rpc.zookeeper.IRegisterCenter4Invoker;
import com.lwh.rpc.zookeeper.RegisterCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 分布式服务框架中客户端在发起服务调用的时候,如何使用软负载算法,选择一个服务提供方发起调用
 *
 *       它是远程服务在服务调用方的动态代理类实现,将复杂的远程调用通信逻辑封装在方法invoke中
 */
public class RevokerProxyBeanFactory implements InvocationHandler {

    private ExecutorService fixedThreadPool = null;

    /**
     * 服务接口
     */
    private Class<?> targetInterface;

    /**
     * 超时时间
     */
    private int consumeTimeout;

    /**
     * 调用者线程数
     */
    private static int threadWorkerNumber = 10;

    /**
     * 负载均衡策略
     */
    private String clusterStrategy;

    private RevokerProxyBeanFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
    }

    private static volatile RevokerProxyBeanFactory singleton;

    public static RevokerProxyBeanFactory getInstance(Class<?> targetInterface, int consumeTimeout, String clusterStrategy){
        if(singleton == null){
            synchronized (RevokerProxyBeanFactory.class){
                if(singleton == null){
                    singleton = new RevokerProxyBeanFactory(targetInterface, consumeTimeout, clusterStrategy);
                }
            }
        }

        return singleton;
    }

    public Object getProxy(){
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //服务接口名称
        String serviceKey = targetInterface.getName();
        //获取某个接口的服务提供者列表
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        List<ProviderService> providerServices = registerCenter4Consumer.getServiceMetaDataMap4Consume().get(serviceKey);
        //根据软负载策略,从服务提供者列表选取本次调用的服务提供者
        ClusterStrategy clusterStrategyService = ClusterEngine.queryClusterStrategy(this.clusterStrategy);
        ProviderService providerService = clusterStrategyService.select(providerServices);

        //复制一份服务提供者信息
        ProviderService newProvider = providerService.copy();
        //设置本次调用服务的方法以及接口
        newProvider.setServiceMethod(method);
        newProvider.setServiceItf(targetInterface);

        //声明调用RpcRequest对象,表示发起一次调用所包含的信息
        final RpcRequest request = new RpcRequest();
        //设置本次调用唯一标识
        request.setUniquekey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        //设置本次调用的服务提供者信息
        request.setProviderService(newProvider);
        //设置本次调用的超时时间
        request.setInvokeTimeout(consumeTimeout);
        //设置本次调用的方法名称
        request.setInvokeMethodName(method.getName());
        //设置本次调用的方法参数信息
        request.setArgs(args);

        try {
            //构建用来发起调用的线程池
            if(fixedThreadPool == null){
                synchronized (RevokerProxyBeanFactory.class){
                    if(fixedThreadPool == null){
                        fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
                    }
                }
            }

            //根据服务提供者的ip和port,构建InetSocketAddress对象,标识服务提供者地址
            String serverIp = newProvider.getServerIp();
            int serverPort = newProvider.getServerPort();
            InetSocketAddress address = new InetSocketAddress(serverIp, serverPort);
            //提交本次调用信息到fixedThreadPool,发起调用
            Future<RpcResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(address, request));
            //获取调用的返回结果
            RpcResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            if(response != null){
                return response.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
