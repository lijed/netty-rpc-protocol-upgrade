/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.protocol;

import com.me.learn.rpc.IRegistryService;
import com.me.learn.rpc.ServiceInfo;
import com.me.learn.rpc.core.RpcProtocol;
import com.me.learn.rpc.core.RpcRequest;
import com.me.learn.rpc.handler.RpcClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/17
 **/

@Slf4j
public class NettyClient {

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final Bootstrap bootstrap;
//    private String serverAddress;
//    private int serverPort;

//    public NettyClient(String serverAddress, int serverPort) {
    public NettyClient() {
//        this.serverAddress = serverAddress;
//        this.serverPort = serverPort;

        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcClientInitializer());
    }

    public void sendRequest(RpcProtocol<RpcRequest> rpcProtocol, IRegistryService registryService) throws Exception {
        ServiceInfo serviceInfo = registryService.discovery(rpcProtocol.getContent().getClassName());
        String serverAddress = serviceInfo.getServiceAddress();
        int serverPort = serviceInfo.getServicePort();
        try {
            final ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(serverAddress, serverPort)).sync();
            //验证连接是否成功
            future.addListener(future1 -> {
                if (future.isSuccess()) {
                    log.info("============connect rpc server {} success", serverAddress);
                } else {
                    log.error("===========connect rpc server {} failed", serverAddress);
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });

            log.info("Begin transfer data");
            future.channel().writeAndFlush(rpcProtocol);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
