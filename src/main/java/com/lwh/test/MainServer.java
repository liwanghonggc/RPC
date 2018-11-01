package com.lwh.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 测试发布服务
 */
public class MainServer {

    public static void main(String[] args) {
        //发布服务
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("rpc-server.xml");
        System.out.println("服务发布完成");
    }

}
