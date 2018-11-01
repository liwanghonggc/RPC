package com.lwh.rpc.cluster.impl;

import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 软负载随机算法实现,获得服务提供者列表大小区间的随机数,作为服务提供者列表的索引来获取服务
 */
public class RandomClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        int MAX_LEN = providerServices.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerServices.get(index);
    }
}
