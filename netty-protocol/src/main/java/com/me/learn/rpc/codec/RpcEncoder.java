/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.codec;

import com.me.learn.rpc.core.Header;
import com.me.learn.rpc.core.RpcProtocol;
import com.me.learn.rpc.serial.ISerializer;
import com.me.learn.rpc.serial.SerializerManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf out) throws Exception {
        log.info("============================decode rpc message ==================================");

        Header header = msg.getHeader();
        out.writeShort(header.getMagic());
        out.writeByte(header.getSerialType());
        out.writeByte(header.getReqType());
        out.writeLong(header.getRequestId());

        //获取序列化器
        ISerializer serializer = SerializerManager.getSerializer(header.getSerialType());
        final byte[] content = serializer.serialize(msg.getContent());

        //设置消息的长度
        out.writeInt(content.length);
        //设置消息体的二进制消息
        out.writeBytes(content);

        log.info("============================decode rpc message end==================================");

    }
}
