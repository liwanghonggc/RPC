package com.lwh.rpc.serialization.handler;

import com.lwh.rpc.serialization.common.SerializeType;
import com.lwh.rpc.serialization.engine.SerializeEngine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 基于Netty的编码器,负责将Java对象序列化为字节数组
 */
public class NettyEncoderHandler extends MessageToByteEncoder {

    /**
     * 序列化类型
     */
    private SerializeType serializeType;

    public NettyEncoderHandler(SerializeType serializeType){
        this.serializeType = serializeType;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        //将对象序列化为字节数组
        byte[] data = SerializeEngine.serialize(in, serializeType.getSerializeType());
        //将字节数组(消息体)的长度作为消息头写入,解决半包/粘包问题
        out.writeInt(data.length);
        //写入序列化后得到的字节数组
        out.writeBytes(data);
    }
}
