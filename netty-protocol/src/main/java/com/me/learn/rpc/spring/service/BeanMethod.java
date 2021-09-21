/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanMethod {
    private Object bean;
    private Method method;
}
