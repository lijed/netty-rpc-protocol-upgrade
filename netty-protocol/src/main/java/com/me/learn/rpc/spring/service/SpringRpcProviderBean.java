/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.service;

import com.me.learn.annotation.GpRemoteService;
import com.me.learn.rpc.IRegistryService;
import com.me.learn.rpc.ServiceInfo;
import com.me.learn.rpc.protocol.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@Slf4j
public class SpringRpcProviderBean implements InitializingBean, BeanPostProcessor {
    private String serverAddress;
    private int port;
    private IRegistryService registryService;

    public SpringRpcProviderBean(int port, IRegistryService registryService) throws UnknownHostException {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();;
        this.port = port;

        this.registryService = registryService;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Start to deploy netty server to {} on port {}", serverAddress, port);
        new Thread(() -> new NettyServer(serverAddress, port).startNettyServer()).start();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        //判读server是否被GpRemoteService标记
        final boolean isGpRemoteService = bean.getClass().isAnnotationPresent(GpRemoteService.class);

        if (isGpRemoteService) {
            final Method[] declaredMethods = bean.getClass().getDeclaredMethods();
            //key的格式： 接口的全类名+ "." + 方法名字
            String key;
            final String serviceName = bean.getClass().getInterfaces()[0].getName();
            for (Method declaredMethod : declaredMethods) {
                key = serviceName + "." +  declaredMethod.getName();
                Mediator.beanMethods.put(key, new BeanMethod(bean, declaredMethod));
            }

            //服务注册到注册中心
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceAddress(serverAddress);
            serviceInfo.setServicePort(port);
            serviceInfo.setServiceName(serviceName);

            try {
                registryService.register(serviceInfo);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("register serivce {} failed",serviceName,e);
            }
        }
        return bean;
    }
}
