package com.lwh.test;

/**
 * @author lwh
 * @date 2018-11-01
 * @desp
 */
public class HelloServiceImpl implements HelloService{

    @Override
    public String sayHello(String somebody) {
        return "hello" + somebody + "!";
    }
}
