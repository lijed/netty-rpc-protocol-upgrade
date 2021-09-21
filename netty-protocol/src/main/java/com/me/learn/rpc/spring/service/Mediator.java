/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.service;

import com.me.learn.rpc.core.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 *
 * @Author: Jed li
 * Created: 2021/9/19
 **/
public class Mediator {
    public static Map<String, BeanMethod> beanMethods = new ConcurrentHashMap<>();

    private Mediator() {
    }

    private static volatile Mediator instance;

    public static Mediator getInstance() {
        if (instance == null) {
            synchronized (Mediator.class) {
                if (instance == null) {
                    instance = new Mediator();
                }
            }
        }
        return instance;
    }

    public Object invoke(RpcRequest request) {
        String key = request.getClassName() + "." + request.getMethodName();
        final BeanMethod beanMethod = beanMethods.get(key);
        if (beanMethod != null) {
            try {
                return beanMethod.getMethod().invoke(beanMethod.getBean(), request.getParas());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
