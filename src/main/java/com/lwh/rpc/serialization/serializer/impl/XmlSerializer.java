package com.lwh.rpc.serialization.serializer.impl;

import com.lwh.rpc.serialization.serializer.ISerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp XML序列化的实现方式,基于XStream实现
 */
public class XmlSerializer implements ISerializer {

    /**
     * 初始化XStream对象
     */
    private static final XStream xStream = new XStream(new DomDriver());

    @Override
    public <T> byte[] serialize(T obj) {
        return xStream.toXML(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String xml = new String(data);
        return (T) xStream.fromXML(xml);
    }
}
