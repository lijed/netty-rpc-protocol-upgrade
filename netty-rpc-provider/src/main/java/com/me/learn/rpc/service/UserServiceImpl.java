/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.service;

import com.me.learn.IUserService;
import com.me.learn.User;
import com.me.learn.annotation.GpRemoteService;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
@GpRemoteService
public class UserServiceImpl implements IUserService {
    @Override
    public String saveUser(User user) {
        System.out.println(user.toString());
        return "add user " + user.getUserName() + " successfully";
    }
}
