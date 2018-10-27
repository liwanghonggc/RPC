package com.lwh.serialization.serializer.impl;

import com.lwh.serialization.serializer.ISerializer;

import java.io.*;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 默认的Java序列化机制.Java的序列化主要通过对象输出流java.io.ObjectOutputStream与对象输入流java.io.ObjectInputStream
 *       来实现.其中被序列化的类需要实现java.io.Serializable接口
 *
 *  优点:
 *  1)Java语言自带,无须额外引入第三方依赖
 *  2)与Java语言有天然最好的易用性和亲和性
 *
 *  缺点:
 *  1)只支持Java,不支持跨语言
 *  2)序列化后产生的码流过大,对于引用过深的对象序列化易发生内存OOM异常
 */
public class DefaultJavaSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);

            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
