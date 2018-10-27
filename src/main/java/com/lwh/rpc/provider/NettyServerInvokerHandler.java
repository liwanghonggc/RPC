package com.lwh.rpc.provider;

import com.lwh.rpc.model.RpcRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 1)Netty服务端接收客户端发起的请求字节数组,然后根据NettyDecoderHandler将字节数组解码为对应的Java请求对象.
 *       2)然后根据解码得到的Java请求对象确定服务提供者的接口及方法,根据Java反射调用
 */

@ChannelHandler.Sharable
public class NettyServerInvokerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

    }
}
