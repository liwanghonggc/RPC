package com.lwh.rpc.serialization.handler;

import com.lwh.rpc.serialization.common.SerializeType;
import com.lwh.rpc.serialization.engine.SerializeEngine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 基于Netty的解码器,复制将字节数组解码为Java对象
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {

    /**
     * 解码对象class
     */
    private Class<?> genericClass;

    /**
     * 解码对象编码所用的序列化类型
     */
    private SerializeType serializeType;

    public NettyDecoderHandler(Class<?> genericClass, SerializeType serializeType) {
        this.genericClass = genericClass;
        this.serializeType = serializeType;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //获取消息头所标识的消息体字节数组长度
        if(in.readableBytes() < 4){
            return;
        }

        in.markReaderIndex();
        int dataLength = in.readInt();
        if(dataLength < 0){
            ctx.close();
        }

        //若当前可以获取到的字节数小于实际长度,则直接返回,直到当前可以获取到的字节数等于实际长度
        if(in.readableBytes() < dataLength){
            in.resetReaderIndex();
            return;
        }

        //读取完整的消息体字节数组
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        //将字节数组反序列化为Java对象
        Object obj = SerializeEngine.deserialize(data, genericClass, serializeType.getSerializeType());
        out.add(obj);
    }
}
