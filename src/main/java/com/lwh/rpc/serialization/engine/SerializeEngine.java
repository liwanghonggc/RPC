package com.lwh.rpc.serialization.engine;

import com.google.common.collect.Maps;
import com.lwh.rpc.serialization.common.SerializeType;
import com.lwh.rpc.serialization.serializer.ISerializer;
import com.lwh.rpc.serialization.serializer.impl.*;

import java.util.Map;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 根据各种序列化实现方案,整合为通用的序列化工具引擎,可以通过输入不同的配置随意选择使用哪一种序列化方案
 */
public class SerializeEngine {

    public static final Map<SerializeType, ISerializer> serializerMap = Maps.newConcurrentMap();

    /**
     * 注册序列化工具类到serializerMap
     */
    static {
        serializerMap.put(SerializeType.DefaultJavaSerializer, new DefaultJavaSerializer());
        serializerMap.put(SerializeType.JackSonSerializer, new JackSonSerializer());
        serializerMap.put(SerializeType.FastJsonSerializer, new FastJsonSerializer());
        serializerMap.put(SerializeType.XmlSerializer, new XmlSerializer());
        serializerMap.put(SerializeType.Xml2Serializer, new Xml2Serializer());
    }

    /**
     * 序列化
     * @param obj
     * @param serializeType
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj, String serializeType){
        SerializeType serialize = SerializeType.queryByType(serializeType);
        if(serialize == null){
            throw new RuntimeException("serializeType is null");
        }

        ISerializer serializer = serializerMap.get(serialize);
        if(serializer == null){
            throw new RuntimeException("serialize error");
        }

        try {
            return serializer.serialize(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz, String serializeType){
        SerializeType serialize = SerializeType.queryByType(serializeType);
        if(serialize == null){
            throw new RuntimeException("serializeType is null");
        }

        ISerializer serializer = serializerMap.get(serialize);
        if(serializer == null){
            throw new RuntimeException("serialize error");
        }

        try {
            return serializer.deserialize(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
