/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.loadbalance;

import com.me.learn.rpc.ServiceInfo;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
public class RandomLoadBalance extends AbstractLoadBalancer {
    @Override
    protected ServiceInstance<ServiceInfo> doSelect(List<ServiceInstance<ServiceInfo>> services) {
        int totalServiceCount = services.size();
        Random random = new Random();
        return services.get(random.nextInt(totalServiceCount));
    }
}
