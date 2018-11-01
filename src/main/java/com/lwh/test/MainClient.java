package com.lwh.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp 测试引入服务
 */
public class MainClient {

    public static void main(String[] args) {
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("rpc-client.xml");
        final HelloService helloService = (HelloService)ctx.getBean("remoteHelloService");

        long count = 100L;

        //调用服务并打印结果
        for(int i = 0; i < count; i++){
            try {
                String result = helloService.sayHello("Hello lwh, i = " + i);
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //关闭jvm
        System.exit(0);
    }
}
