/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.referrence;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
@Configuration
@EnableConfigurationProperties(RpcClientProperties.class)
public class SpringRpcReferrenceAutoConfiguration  implements EnvironmentAware {

    private Environment environment;

//    @Bean
//    public SpringRpcReferrencePostProcessor recReferencePostProcessor(RpcClientProperties rpcClientProperties) {
//        return new SpringRpcReferrencePostProcessor(rpcClientProperties);
//    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SpringRpcReferrencePostProcessor referrencePostProcessor() {
        RpcClientProperties rpcClientProperties = new RpcClientProperties();
//        rpcClientProperties.setServiceAddress(environment.getProperty("gp.client.serviceAddress"));
//        rpcClientProperties.setServicePort(Integer.valueOf(environment.getProperty("gp.client.servicePort")));
        rpcClientProperties.setRegistryAddress(environment.getProperty("gp.client.registryAddress"));
        rpcClientProperties.setRegistryType(Byte.parseByte(environment.getProperty("gp.client.registryType")));
        SpringRpcReferrencePostProcessor springRpcReferrencePostProcessor = new SpringRpcReferrencePostProcessor(rpcClientProperties);
        return springRpcReferrencePostProcessor;
    }
}
