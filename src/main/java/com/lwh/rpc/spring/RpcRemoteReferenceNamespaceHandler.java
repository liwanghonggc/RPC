package com.lwh.rpc.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author lwh
 * @date 2018-11-01
 *
 */
public class RpcRemoteReferenceNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new RevokerFactoryBeanDefinitionParser());
    }
}
