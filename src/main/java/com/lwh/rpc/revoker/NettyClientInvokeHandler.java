package com.lwh.rpc.revoker;

import com.lwh.rpc.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author lwh
 * @date 2018-10-31
 * @desp 我们在Channel的连接池工厂类的具体实现NettyChannelPoolFactory中实现客户端具体业务逻辑处理器
 *       NettyClientInvokeHandler,在NettyClientInvokerHandler中获取Netty异步调用返回的结果,并
 *       将该结果保存到RevokerResponseHandler
 */
public class NettyClientInvokeHandler extends SimpleChannelInboundHandler<RpcResponse> {

    public NettyClientInvokeHandler(){

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        //将Netty异步返回的结果存入阻塞队列,以便调用端同步获取
        RevokerResponseHolder.putResultValue(response);
    }

}
