/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.serial;

import com.alibaba.fastjson.JSON;
import com.me.learn.rpc.contants.SerialType;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
public class JsonSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        return JSON.toJSONString(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) JSON.parseObject(new String(data), clazz);
    }

    @Override
    public byte getType() {
        return SerialType.JSON_SERIAL.code();
    }
}
