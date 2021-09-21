/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.referrence;

import com.me.learn.rpc.IRegistryService;
import com.me.learn.rpc.contants.ReqType;
import com.me.learn.rpc.contants.RpcConstant;
import com.me.learn.rpc.contants.SerialType;
import com.me.learn.rpc.core.*;
import com.me.learn.rpc.protocol.NettyClient;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Description:
 *
 * @Author: Jed
 * Created: 2021/9/17
 **/
public class RpcClientInvokerProxy<T> implements InvocationHandler {

    //服务注册，用来重注册中心获取服务的地址
    private IRegistryService registryService;

    public RpcClientInvokerProxy(IRegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        for (Method decMethod: method.getDeclaringClass().getDeclaredMethods()) {
            if (!method.equals(decMethod)){
                return method.invoke(proxy, args);
            }
        }

        //构建RpcProtocol
        RpcProtocol<RpcRequest> rpcProtocol = new RpcProtocol<RpcRequest>();

        Header header = new Header();
        header.setMagic(RpcConstant.MAGIC);
        header.setSerialType(SerialType.JSON_SERIAL.code());
        header.setReqType(ReqType.REQUEST.code());
        final Long requestId = RequestHolder.REQUEST_ID.incrementAndGet();
        header.setRequestId(requestId);

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParas(args);

        rpcProtocol.setHeader(header);
        rpcProtocol.setContent(rpcRequest);
        //send 消息到服务端
        NettyClient nettyClient = new NettyClient();

        //在异步收到服务端请求后，通知client - future.getPromise().get().getData();
        RpcFuture<RpcResponse> future= new RpcFuture<RpcResponse>(new DefaultPromise<RpcResponse>(new DefaultEventLoop()));
        RequestHolder.REQUEST_MAP.put(requestId, future);

        nettyClient.sendRequest(rpcProtocol, registryService);

        //阻塞获取消息的response并返回
        return future.getPromise().get().getData();
    }
}
