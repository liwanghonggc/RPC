package com.lwh.serialization.serializer.impl;

import com.lwh.serialization.serializer.ISerializer;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp Java自带的实现XML序列化/反序列化主要是使用java.beans.XMLEncoder与java.beans.XMLDecoder
 *       来完成
 */
public class Xml2Serializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder xmlEncoder = new XMLEncoder(out, "utf-8", true, 0);
        xmlEncoder.writeObject(obj);
        xmlEncoder.close();
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(data));
        Object obj = xmlDecoder.readObject();
        xmlDecoder.close();
        return (T) obj;
    }
}
