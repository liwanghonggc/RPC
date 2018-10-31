package com.lwh.rpc.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author lwh
 * @date 2018-10-31
 * @desp Netty异步调用返回结果包装类
 *
 *       Netty是异步框架,客户端发起服务调用后同步等待获取调用结果
 *
 *           Netty是异步编程框架,客户端发起请求之后,不会同步等待结果返回,需要自己实现同步等待机制,实现思路为:为每次请求新建一个
 *       阻塞队列,返回结果的时候,存入该阻塞队列,若在超时时间内返回结果值,则调用端将该返回结果队列从阻塞队列中取回返回给调
 *       用方,否则超时返回null
 *
 *       定义一个Netty调用返回结果的包装类RpcResponseWrapper,由保存返回结果的阻塞队列BlockingQueue与返回时间responseTime
 *       组成,同时定义了判断返回结果是否超时过期的方法isExpire()
 */
public class RpcResponseWrapper {

    /**
     * 存储返回结果的阻塞队列
     */
    private BlockingQueue<RpcResponse> responseQueue = new ArrayBlockingQueue<>(1);

    /**
     * 结果返回时间
     */
    private long responseTime;

    /**
     * 计算该结果返回时间是否已经过期
     * @return
     */
    public boolean isExpire(){
        RpcResponse response = responseQueue.peek();
        if(response == null){
            return false;
        }

        long timeout = response.getInvokeTimeout();
        if((System.currentTimeMillis() - responseTime) > timeout){
            return true;
        }
        return false;
    }

    public static RpcResponseWrapper of(){
        return new RpcResponseWrapper();
    }

    public BlockingQueue<RpcResponse> getResponseQueue() {
        return responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
