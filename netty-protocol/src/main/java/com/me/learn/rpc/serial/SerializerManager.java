/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.serial;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 *
 * @Author: Jed
 * Created: 2021/9/16
 **/
public class SerializerManager {

    private static final Map<Byte, ISerializer> SERIALIZER_HASH_MAP = new ConcurrentHashMap<>();

    static {
        ISerializer json = new JsonSerializer();
        ISerializer java = new JavaSerializer();
        SERIALIZER_HASH_MAP.put(json.getType(), json);
        SERIALIZER_HASH_MAP.put(java.getType(), java);
    }

    public static ISerializer getSerializer(byte key) {
        ISerializer iSerializer = SERIALIZER_HASH_MAP.get(key);
        if (iSerializer == null) {
            iSerializer = new JavaSerializer();
        }
        return iSerializer;
    }
}
