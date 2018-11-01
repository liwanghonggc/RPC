package com.lwh.rpc.cluster;

import com.lwh.rpc.model.ProviderService;

import java.util.List;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 负载均衡算法定义接口
 */
public interface ClusterStrategy {

    /**
     * 选择负载策略算法
     * @param providerServices 服务提供者列表
     * @return
     */
    ProviderService select(List<ProviderService> providerServices);
}
