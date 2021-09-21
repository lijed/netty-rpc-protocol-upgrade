/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.service;

import com.me.learn.ICustomerService;
import com.me.learn.annotation.GpRemoteService;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
@Slf4j
@GpRemoteService
public class CustomerServiceImpl implements ICustomerService {
    @Override
    public String createCustomer(String userName) {
        log.info("Add customer {} success", userName);
        return "succcessfully add customer " +  userName;
    }
}
