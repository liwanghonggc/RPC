package com.lwh.rpc.cluster.impl;

import com.google.common.collect.Lists;
import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.model.ProviderService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 软负载实现加权轮询算法,在轮询算法的基础上对权重做了处理
 */
public class WeightPollingClusterStrategyImpl implements ClusterStrategy {

    //计数器
    private int index = 0;

    private Lock lock = new ReentrantLock();

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        ProviderService service = null;
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            //存放加权后的服务提供者列表
            List<ProviderService> providerList = Lists.newArrayList();
            for(ProviderService provider : providerServices){
                int weight = provider.getWeight();
                for(int i = 0; i < weight; i++){
                    providerList.add(provider.copy());
                }
            }

            //若计数个数大于提供者个数,计数器归零
            if(index > providerList.size()){
                index = 0;
            }

            service = providerList.get(index);
            index++;
            return service;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //兜底,保证程序健壮性,若未取到服务,直接取第一个
        return providerServices.get(0);
    }
}
