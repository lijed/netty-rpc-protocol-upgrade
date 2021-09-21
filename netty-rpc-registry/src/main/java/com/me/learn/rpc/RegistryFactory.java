/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc;

import com.me.learn.rpc.zookeeper.ZookeeperRegistryService;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
public class RegistryFactory {

    public static IRegistryService createRegistryService(String address, RegistryType registryType) {
        IRegistryService service = null;
        try {
            switch (registryType) {
                case EUREKA:
                    break;
                case ZOOKEEPER:
                default:
                    service = new ZookeeperRegistryService(address);
                    break;
            }

            return service;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}
