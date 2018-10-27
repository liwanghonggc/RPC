package com.lwh.rpc.serialization.serializer;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 序列化/反序列化通用接口
 */
public interface ISerializer {

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
