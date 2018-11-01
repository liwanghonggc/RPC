package com.lwh.rpc.revoker;

import com.google.common.collect.Maps;
import com.lwh.rpc.model.RpcResponse;
import com.lwh.rpc.model.RpcResponseWrapper;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author lwh
 * @date 2018-10-31
 * @desp 保存及操作返回结果的数据容器类RevokerResponseHolder
 */
public class RevokerResponseHolder {

    /**
     * 服务返回结果的Map
     */
    private static final Map<String, RpcResponseWrapper> responseMap = Maps.newConcurrentMap();

    /**
     * 清除过期的返回结果
     */
    private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

    static {
        //删除超时未获取到结果的key,防止内存泄露
        removeExpireKeyExecutor.execute(() -> {
            while (true){
                try {
                    for(Map.Entry<String, RpcResponseWrapper> entry : responseMap.entrySet()){
                        boolean isExpire = entry.getValue().isExpire();
                        if(isExpire){
                            responseMap.remove(entry.getKey());
                        }
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 初始化返回结果容器,requestUniqueKey唯一标识本次调用
     * @param requestUniqueKey
     */
    public static void initResponseData(String requestUniqueKey){
        responseMap.put(requestUniqueKey, RpcResponseWrapper.of());
    }

    /**
     * 将Netty调用异步返回结果放入阻塞队列
     * @param response
     */
    public static void putResultValue(RpcResponse response){
        long currentTime = System.currentTimeMillis();
        RpcResponseWrapper rpcResponseWrapper = responseMap.get(response.getUniqueKey());
        rpcResponseWrapper.setResponseTime(currentTime);
        rpcResponseWrapper.getResponseQueue().add(response);
        responseMap.put(response.getUniqueKey(), rpcResponseWrapper);
    }

    /**
     * 从阻塞队列中获取Netty异步返回的结果值
     * @param requestUniqueKey
     * @param timeout
     * @return
     */
    public static RpcResponse getValue(String requestUniqueKey, long timeout){
        RpcResponseWrapper responseWrapper = responseMap.get(requestUniqueKey);
        try {
            return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            responseMap.remove(requestUniqueKey);
        }
    }
}
