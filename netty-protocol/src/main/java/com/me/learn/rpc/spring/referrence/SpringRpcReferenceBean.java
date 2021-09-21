/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.referrence;

import com.me.learn.rpc.IRegistryService;
import com.me.learn.rpc.RegistryFactory;
import com.me.learn.rpc.RegistryType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * Description:
 * 工厂Bean用于构建复杂的bean
 * @Author: Administrator
 * Created: 2021/9/19
 **/
public class SpringRpcReferenceBean<T> implements FactoryBean<T> {

    private Object object;
    private Class<?> serviceClass;
    private String registryAddress;
    private byte registryType;


    public SpringRpcReferenceBean() {}

    /**
     *  //进一步优化：cglib 或者javaassitent
     */
    public void init() {
        IRegistryService registryService = RegistryFactory.createRegistryService(registryAddress, RegistryType.findByCode(registryType));
        this.object = (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass},
                new RpcClientInvokerProxy(registryService));
    }

    @Override
    public T getObject() throws Exception {
        return (T) this.object;
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setRegistryType(byte registryType) {
        this.registryType = registryType;
    }
}
