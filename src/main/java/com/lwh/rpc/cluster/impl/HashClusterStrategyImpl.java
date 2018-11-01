package com.lwh.rpc.cluster.impl;

import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.helper.IPHelper;
import com.lwh.rpc.model.ProviderService;

import java.util.List;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 软负载源地址hash算法实现
 */
public class HashClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //获取调用方IP
        String localIP = IPHelper.localIp();
        //获取源地址对应的hashCode
        int hashCode = localIP.hashCode();
        //获取服务列表大小
        int size = providerServices.size();

        return providerServices.get(hashCode % size);
    }
}
