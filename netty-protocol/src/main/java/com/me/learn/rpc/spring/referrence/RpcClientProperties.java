/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.referrence;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@Data
@ConfigurationProperties(prefix = "gp.client")
public class RpcClientProperties {
    private String serviceAddress;
    private int servicePort;

    private String registryAddress;
    private byte registryType;

}
