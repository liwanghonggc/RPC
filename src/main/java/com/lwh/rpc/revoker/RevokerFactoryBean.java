package com.lwh.rpc.revoker;

import com.lwh.rpc.model.InvokerService;
import com.lwh.rpc.model.ProviderService;
import com.lwh.rpc.zookeeper.IRegisterCenter4Invoker;
import com.lwh.rpc.zookeeper.RegisterCenter;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 实现远程服务的引入
 *
 *       引入远程服务需要做的操作如下:
 *       1.通过注册中心,将服务提供者的信息获取到本地缓存
 *       2.初始化Netty连接池
 *       3.获取服务提供者代理对象
 *       4.将服务消费者信息注册到注册中心
 */
public class RevokerFactoryBean implements FactoryBean, InitializingBean {

    /**
     * 服务接口
     */
    private Class<?> targetInterface;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 服务bean
     */
    private Object serviceObject;

    /**
     * 负载均衡策略
     */
    private String clusterStrategy;

    /**
     * 服务提供者唯一标识
     */
    private String remoteAppKey;

    /**
     * 服务分组组名
     */
    private String groupName = "group";

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //获取服务注册中心
        IRegisterCenter4Invoker registerCenter4Invoker = RegisterCenter.singleton();
        //初始化服务提供者列表到本地缓存
        registerCenter4Invoker.initProviderMap(remoteAppKey, groupName);

        //初始化Netty Channel
        Map<String, List<ProviderService>> providerMap = registerCenter4Invoker.getServiceMetaDataMap4Consume();
        if(MapUtils.isEmpty(providerMap)){
            throw new RuntimeException("service provider list is empty");
        }

        NettyChannelPoolFactory.getInstance().initChannelPoolFactory(providerMap);

        //获取服务提供者代理对象
        RevokerProxyBeanFactory proxyFactory = RevokerProxyBeanFactory.getInstance(targetInterface, timeout, clusterStrategy);
        this.serviceObject = proxyFactory.getProxy();

        //将消费者信息注册到注册中心
        InvokerService invokerService = new InvokerService();
        invokerService.setServiceItf(targetInterface);
        invokerService.setRemoteAppKey(remoteAppKey);
        invokerService.setGroupName(groupName);
        registerCenter4Invoker.registerInvoker(invokerService);
    }

    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(String clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
