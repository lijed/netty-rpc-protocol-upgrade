/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.core;

import lombok.Data;

import java.io.Serializable;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/

@Data
public class RpcProtocol<T> implements Serializable {
    private Header header;
    private T content;
}
