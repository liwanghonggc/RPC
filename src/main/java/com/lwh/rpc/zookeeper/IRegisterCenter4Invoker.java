package com.lwh.rpc.zookeeper;

import com.lwh.rpc.model.InvokerService;
import com.lwh.rpc.model.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 消费端注册中心接口
 */
public interface IRegisterCenter4Invoker {

    /**
     * 消费端初始化服务提供者信息本地缓存
     * @param remoteAppKey
     * @param groupName
     */
    void initProviderMap(String remoteAppKey, String groupName);

    /**
     * 消费端获取服务提供者信息
     * @return
     */
    Map<String, List<ProviderService>> getServiceMetaDataMap4Consume();

    /**
     * 消费端将消费注册信息注册到zk对应节点下
     * @param invoker
     */
    void registerInvoker(final InvokerService invoker);
}
