package com.lwh.rpc.provider;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp
 */
public class NettyServer {

    private static NettyServer nettyServer = new NettyServer();

    private Channel channel;

    /**
     * 服务端boss线程组
     */
    private EventLoopGroup bossGroup;

    /**
     * 服务端worker线程组
     */
    private EventLoopGroup workerGroup;
}
