package com.lwh.rpc.revoker;

import com.lwh.rpc.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author lwh
 * @date 2018-10-31
 * @desp
 */
public class NettyClientInvokerHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {

    }

    //TODO
}
