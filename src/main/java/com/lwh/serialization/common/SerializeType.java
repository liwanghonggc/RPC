package com.lwh.serialization.common;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 序列化类型枚举
 */
public enum SerializeType {

    /**
     * 对应序列化类型,分别是Java默认序列化、FastJson、Jackson及两种XML实现
     */
    DefaultJavaSerializer("DefaultJavaSerializer"),
    FastJsonSerializer("FastJsonSerializer"),
    JackSonSerializer("JackSonSerializer"),
    Xml2Serializer("Xml2Serializer"),
    XmlSerializer("XmlSerializer");

    private String serializeType;

    SerializeType(String serializeType){
        this.serializeType = serializeType;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public static SerializeType queryByType(String serializeType){
        if(StringUtils.isBlank(serializeType)){
            return DefaultJavaSerializer;
        }

        for(SerializeType serialize : SerializeType.values()){
            if(StringUtils.equals(serializeType, serialize.getSerializeType())){
                return  serialize;
            }
        }

        return DefaultJavaSerializer;
    }
}
