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

import java.util.List;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
public interface ILoadBalancer {

    ServiceInstance<ServiceInfo> select(List<ServiceInstance<ServiceInfo>> services);
}
