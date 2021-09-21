/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.controller;

import com.me.learn.ICustomerService;
import com.me.learn.IUserService;
import com.me.learn.User;
import com.me.learn.annotation.GpRemoteReferrence;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@RestController
public class TestController {

    @GpRemoteReferrence
    private ICustomerService customerService;


    @GpRemoteReferrence
    private IUserService userService;

    @GetMapping("/customer/add")
    public String saveCUtomerService() {
        return customerService.createCustomer("JedLI");
    }


    @GetMapping("/user")
    public String saveUser() {
        User user= new User();
        user.setUserName("Jed Li");
        user.setAge(38);
        return userService.saveUser(user);
    }
}
