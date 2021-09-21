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
public enum RegistryType {
    ZOOKEEPER((byte)0),
    EUREKA((byte)1);

    private byte code;

    private RegistryType(byte code) {
        this.code = code;
    }

    public byte code() {
        return this.code;
    }

    public static RegistryType findByCode(byte code) {
        for (RegistryType registryType : RegistryType.values()) {
            if (registryType.code == code) {
                return registryType;
            }
        }
        return null;
    }
}
