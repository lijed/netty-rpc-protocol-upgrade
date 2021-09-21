/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.contants;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
public enum  ReqType {
    REQUEST((byte)1),
    RESPONSE((byte)2),
    HEARTBEAT((byte)3);

    private byte code;
    ReqType(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }

    public static ReqType findByCode(byte code) {
        for (ReqType reqType : ReqType.values()) {
            if (reqType.code == code) {
                return reqType;
            }
        }
        return null;
    }
}
