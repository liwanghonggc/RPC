package com.lwh.rpc.cluster.impl;

import com.google.common.collect.Lists;
import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 软负载加权随机算法实现,在随机算法基础上针对权重做了处理
 */
public class WeightRandomClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //存放加权后的服务提供者列表
        List<ProviderService> providerList = Lists.newArrayList();
        for(ProviderService provider : providerServices){
            int weight = provider.getWeight();
            for(int i = 0; i < weight; i++){
                providerList.add(provider.copy());
            }
        }

        int MAX_LEN = providerList.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerList.get(index);
    }
}
