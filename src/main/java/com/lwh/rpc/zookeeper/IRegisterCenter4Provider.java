package com.lwh.rpc.zookeeper;

import com.lwh.rpc.model.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 服务端注册中心接口
 */
public interface IRegisterCenter4Provider {

    /**
     * 服务端将服务提供者信息注册到zk对应节点下,注册完以后会使用ZK的监听机制,若服务列表有变化会同步到本地缓存
     * @param serviceMetaData
     */
    void registerProvider(final List<ProviderService> serviceMetaData);

    /**
     * 服务端获取服务提供者信息
     * 返回对象
     * key:服务提供者接口
     * value:服务提供者服务方法列表
     * @return
     */
    Map<String, List<ProviderService>> getProviderServiceMap();
}
