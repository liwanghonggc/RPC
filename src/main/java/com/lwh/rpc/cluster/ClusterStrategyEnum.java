package com.lwh.rpc.cluster;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp
 */
public enum ClusterStrategyEnum {

    /**
     * 随机,权重随机,轮询,权重轮询,源地址hash
     */
    Random("Random"),
    WeightRandom("WeightRandom"),
    Polling("Polling"),
    WeightPolling("WeightPolling"),
    Hash("Hash");

    private ClusterStrategyEnum(String code){
        this.code = code;
    }

    public static ClusterStrategyEnum queryByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        for(ClusterStrategyEnum strategy : values()){
            if(StringUtils.equals(code, strategy.getCode())){
                return strategy;
            }
        }

        return null;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
