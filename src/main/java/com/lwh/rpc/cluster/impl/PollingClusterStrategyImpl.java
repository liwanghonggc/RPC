package com.lwh.rpc.cluster.impl;

import com.lwh.rpc.cluster.ClusterStrategy;
import com.lwh.rpc.model.ProviderService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 软负载轮询算法,轮询算法,将服务调用请求按顺序轮流分配到服务提供者后端服务器上,均衡对待每一台服务提供者机器
 */
public class PollingClusterStrategyImpl implements ClusterStrategy {

    //计数器
    private int index = 0;

    private Lock lock = new ReentrantLock();

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        ProviderService service = null;
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            //若计数大于服务提供者个数,将计数器归零
            if(index >= providerServices.size()){
                index = 0;
            }

            service = providerServices.get(index);
            index++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //兜底,保证程序健壮性,若未取到服务,则直接取第一个
        if (service == null) {
            service = providerServices.get(0);
        }

        return service;
    }
}
