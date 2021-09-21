/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


//把com.me.learn.rpc.spring.service,让nettyserver能自动启动
@ComponentScan(basePackages = {"com.me.learn.rpc.spring.service", "com.me.learn.rpc.service"})
@SpringBootApplication
public class NettyRpcProvider {
    public static void main(String[] args) {
        SpringApplication.run(NettyRpcProvider.class, args);
    }
}
