package com.lwh.rpc.provider;

import com.lwh.rpc.helper.PropertyConfigureHelper;
import com.lwh.rpc.model.RpcRequest;
import com.lwh.rpc.serialization.common.SerializeType;
import com.lwh.rpc.serialization.handler.NettyDecoderHandler;
import com.lwh.rpc.serialization.handler.NettyEncoderHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

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

    /**
     * 序列化类型配置信息
     */
    private SerializeType serializeType = PropertyConfigureHelper.getSerializeType();

    /**
     * 启动Netty服务
     */
    public void start(final int port){
        if(bossGroup != null || workerGroup != null){
            return;
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        //注册解码器
                        pipeline.addLast(new NettyDecoderHandler(RpcRequest.class, serializeType));
                        //注册编码器
                        pipeline.addLast(new NettyEncoderHandler(serializeType));
                        //注册服务端业务逻辑处理器
                        pipeline.addLast(new NettyServerInvokerHandler());

                    }
                });

        try {
            channel = serverBootstrap.bind(port).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 停止Netty服务
     */
    public void stop(){
        if(channel == null){
            throw new RuntimeException("Netty Server Stopped!");
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }

    private NettyServer(){

    }

    public static NettyServer singleton(){
        return nettyServer;
    }
}
