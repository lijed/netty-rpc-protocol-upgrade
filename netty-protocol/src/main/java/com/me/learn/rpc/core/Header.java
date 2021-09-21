/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Header {
    private short magic; //魔数 2字节
    private byte serialType;  //序列化类型  1个字节
    private byte reqType;  //消息类型  1个字节
    private long requestId; //请求id  8个字节
    private int length;  //消息体长度，4个字节
}
