/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/17
 **/
public class RequestHolder {

    public static final AtomicLong REQUEST_ID = new AtomicLong();

    public static final Map<Long, RpcFuture> REQUEST_MAP = new ConcurrentHashMap<>();

}
