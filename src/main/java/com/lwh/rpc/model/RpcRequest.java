package com.lwh.rpc.model;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp RPC请求类
 */
public class RpcRequest {

    /**
     * UUID,唯一标识一次返回值
     */
    private String uniquekey;

    /**
     * 服务提供者信息
     */
    private ProviderService providerService;

    /**
     * 调用的方法名称
     */
    private String invokeMethodName;

    /**
     * 传递参数
     */
    private Object[] args;

    /**
     * 消费端应用名
     */
    private String appName;

    /**
     * 消费请求超时时长
     */
    private long invokeTimeout;

    public String getUniquekey() {
        return uniquekey;
    }

    public void setUniquekey(String uniquekey) {
        this.uniquekey = uniquekey;
    }

    public ProviderService getProviderService() {
        return providerService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public String getInvokeMethodName() {
        return invokeMethodName;
    }

    public void setInvokeMethodName(String invokeMethodName) {
        this.invokeMethodName = invokeMethodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getInvokeTimeout() {
        return invokeTimeout;
    }

    public void setInvokeTimeout(long invokeTimeout) {
        this.invokeTimeout = invokeTimeout;
    }
}
