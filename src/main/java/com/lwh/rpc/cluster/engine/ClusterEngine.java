package com.lwh.rpc.cluster.engine;

import com.google.common.collect.Maps;
import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.cluster.ClusterStrategyEnum;
import com.lwh.rpc.cluster.impl.*;

import java.util.Map;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 负载均衡引擎,将负载均衡算法整合到我们的分布式服务框架的实现中
 */
public class ClusterEngine {

    private static final Map<ClusterStrategyEnum, ClusterStrategy> clusterStrategyMap = Maps.newConcurrentMap();

    static {
        clusterStrategyMap.put(ClusterStrategyEnum.Random, new RandomClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.WeightRandom, new WeightRandomClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.Polling, new PollingClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.WeightPolling, new WeightPollingClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.Hash, new HashClusterStrategyImpl());
    }

    public static ClusterStrategy queryClusterStrategy(String clusterStrategy){
        ClusterStrategyEnum clusterStrategyEnum = ClusterStrategyEnum.queryByCode(clusterStrategy);
        if(clusterStrategyEnum == null){
            //选择默认的随机算法
            return new RandomClusterStrategyImpl();
        }

        return clusterStrategyMap.get(clusterStrategyEnum);
    }
}
