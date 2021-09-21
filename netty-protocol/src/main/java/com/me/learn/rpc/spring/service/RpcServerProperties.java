/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@Data
@ConfigurationProperties(prefix = "gp.rpc")
public class RpcServerProperties {
    private int servicePort;

    //注册中心的地址
    private String registryAddress;

    //注册中心的类型
    private byte registryType;

}
