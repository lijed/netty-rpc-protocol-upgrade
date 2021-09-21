/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.handler;

import com.me.learn.rpc.contants.ReqType;
import com.me.learn.rpc.core.Header;
import com.me.learn.rpc.core.RpcProtocol;
import com.me.learn.rpc.core.RpcRequest;
import com.me.learn.rpc.core.RpcResponse;
import com.me.learn.rpc.spring.SpringBeanManager;
import com.me.learn.rpc.spring.service.Mediator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> msg) throws Exception {

        //首先拿到消息内容
        final RpcRequest content = msg.getContent();

        final Header header = msg.getHeader();
        header.setReqType(ReqType.RESPONSE.code());

        //调用服务提供者
        Object result = invoke(msg.getContent());

        RpcProtocol<RpcResponse> rpcProtocol = new RpcProtocol<>();

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setData(result);
        rpcResponse.setMsg("success");

        rpcProtocol.setHeader(header);
        rpcProtocol.setContent(rpcResponse);

        ctx.writeAndFlush(rpcProtocol);
    }

    private Object invoke(RpcRequest rpcRequest) {
        return  Mediator.getInstance().invoke(rpcRequest);
    }
}
