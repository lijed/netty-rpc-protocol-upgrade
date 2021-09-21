/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.handler;

import com.me.learn.rpc.core.RequestHolder;
import com.me.learn.rpc.core.RpcFuture;
import com.me.learn.rpc.core.RpcProtocol;
import com.me.learn.rpc.core.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/17
 **/

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) throws Exception {
        log.info("receive Rpc Server Result");
        Long requestId = msg.getHeader().getRequestId();
        RpcFuture<RpcResponse> future  = RequestHolder.REQUEST_MAP.remove(requestId);
        //返回结果
        log.info("服务端的返回结果"+ msg.getContent());
        future.getPromise().setSuccess(msg.getContent());
    }
}
