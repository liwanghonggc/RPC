package com.lwh.rpc.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * @author lwh
 * @date 2018-10-29
 * @desp
 */
public class IPHelper {

    private static final Logger logger = LoggerFactory.getLogger(IPHelper.class);

    private static String hostIp = StringUtils.EMPTY;

    /**
     * 获取本机IP,通过获取系统所有的networkInterface网络接口,然后遍历每个网络下的InterfaceAddress组.
     * 获得符合InetAddress instanceof Inet4Address条件的一个IPV4地址
     */
    static {
        String ip = null;
        Enumeration allNetInterfaces;

        try{
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = (NetworkInterface)allNetInterfaces.nextElement();
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for(InterfaceAddress address : interfaceAddresses){
                    InetAddress ipAddr = address.getAddress();
                    if(ipAddr != null && ipAddr instanceof Inet4Address){
                        if(StringUtils.equals(ipAddr.getHostAddress(), "127.0.0.1")){
                            continue;
                        }
                        ip = ipAddr.getHostAddress();
                        break;
                    }
                }
            }
        }catch (SocketException e){
            logger.warn("获取本机IP地址失败:异常信息: " + e.getMessage());
            System.out.println("获取本机IP地址失败:异常信息: " + e.getMessage());
            throw new RuntimeException(e);
        }

        hostIp = ip;
    }

    public static String localIp(){
        return hostIp;
    }
}
