/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.core;

import io.netty.util.concurrent.Promise;
import lombok.Data;

/**
 * Description:
 *
 * @Author: Jed Li
 * Created: 2021/9/17
 **/

@Data
public class RpcFuture<T> {

    private Promise<T> promise;

    public RpcFuture(Promise<T> promise) {
        this.promise = promise;
    }
}
