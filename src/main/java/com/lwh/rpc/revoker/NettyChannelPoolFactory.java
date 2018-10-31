package com.lwh.rpc.revoker;

import com.lwh.rpc.model.ProviderService;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author lwh
 * @date 2018-10-31
 * @desp Channel连接池工厂类
 *       Netty客户端发起调用,重点解决的问题有三个
 *       1)选择合适的序列化协议,解决Netty传输过程中出现的半包/粘包问题
 *       2)发挥长连接优势,对Netty的Channel通道进行复用
 *       3)Netty是异步框架,客户端发起服务调用后同步等待获取调用结果
 *
 *       此处解决问题2,为了使得Channel能够复用,编写了一个Channel连接池工厂类,针对每一个服务提供者地址,预先生成了一个保存
 *       Channel的阻塞队列
 */
public class NettyChannelPoolFactory {

    /**
     * 初始化Netty channel 连接队列
     * @param providerMap
     */
    public void initChannelPoolFactory(Map<String, List<ProviderService>> providerMap){

    }

    /**
     * 根据服务提供者地址获取对应的Netty Channel阻塞队列
     * @param address
     * @return
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress address){
        return null;
    }

    /**
     * Channel使用完毕之后,回收到阻塞队列arrayBlockingQueue
     * @param arrayBlockingQueue
     * @param channel
     * @param address
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress address){

    }

    /**
     * 为服务提供者地址address注册新的channel
     * @param address
     * @return
     */
    public Channel registerChannel(InetSocketAddress address){
        return null;
    }
}
