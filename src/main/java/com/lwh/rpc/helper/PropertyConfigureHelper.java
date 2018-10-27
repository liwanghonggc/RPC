package com.lwh.rpc.helper;

import com.lwh.rpc.serialization.common.SerializeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp
 */
public class PropertyConfigureHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertyConfigureHelper.class);

    private static final String PROPERTY_PATH = "/rpc_remoting.properties";

    private static final Properties properties = new Properties();

    /**
     * ZK服务地址
     */
    private static String zkService = "";

    /**
     *ZK Session超时时间
     */
    private static int zkSessionTimeout;

    /**
     *ZK connection超时时间
     */
    private static int zkConnectionTimeout;

    /**
     *序列化算法类型
     */
    private static SerializeType serializeType;

    /**
     *每个服务端提供者的Netty连接数
     */
    private static int channelConnectSize;

    /**
     * 初始化
     */
    static {
        InputStream is = null;
        try {
            is = PropertyConfigureHelper.class.getResourceAsStream(PROPERTY_PATH);
            if(is == null){
                throw new IllegalStateException("rpc_remoting.properties can not found in classpath");
            }

            properties.load(is);

            zkService = properties.getProperty("zk_service");
            zkSessionTimeout = Integer.parseInt(properties.getProperty("zk_sessionTimeout", "500"));
            zkConnectionTimeout = Integer.parseInt(properties.getProperty("zk_connectionTimeout", "500"));
            channelConnectSize = Integer.parseInt(properties.getProperty("channel_connect_size", "10"));
            String serialType = properties.getProperty("serial_type");
            serializeType = SerializeType.queryByType(serialType);

            if(serializeType == null){
                throw new RuntimeException("serializeType is null");
            }
        } catch (Throwable t) {
            logger.error("load rpc_remoting.properties file failed", t);
            throw new RuntimeException(t);
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getZkService() {
        return zkService;
    }

    public static int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public static int getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public static SerializeType getSerializeType() {
        return serializeType;
    }

    public static int getChannelConnectSize() {
        return channelConnectSize;
    }
}
