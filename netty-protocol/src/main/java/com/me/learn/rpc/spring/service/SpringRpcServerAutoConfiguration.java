/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.service;

import com.me.learn.rpc.IRegistryService;
import com.me.learn.rpc.RegistryFactory;
import com.me.learn.rpc.RegistryType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class SpringRpcServerAutoConfiguration {

    @Bean
    public SpringRpcProviderBean springRpcProviderBean(RpcServerProperties rpcServerProperties) throws UnknownHostException {
        int port = rpcServerProperties.getServicePort();
        final IRegistryService registryService = RegistryFactory.createRegistryService(
                rpcServerProperties.getRegistryAddress(), RegistryType.findByCode(rpcServerProperties.getRegistryType()));
        return new SpringRpcProviderBean(port, registryService);
    }
}
