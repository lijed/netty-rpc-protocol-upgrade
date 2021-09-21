/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
public interface IRegistryService {

    void register(ServiceInfo serviceInfo) throws Exception;

    ServiceInfo discovery(String serviceName) throws Exception;
}
