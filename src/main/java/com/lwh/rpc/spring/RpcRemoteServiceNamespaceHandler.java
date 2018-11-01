package com.lwh.rpc.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author lwh
 * @date 2018-11-01
 * 服务发布自定义标签
 */
public class RpcRemoteServiceNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ProviderFactoryBeanDefinitionParser());
    }
}
