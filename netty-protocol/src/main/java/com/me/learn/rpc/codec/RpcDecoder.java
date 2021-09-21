/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.codec;

import com.alibaba.fastjson.JSON;
import com.me.learn.rpc.contants.ReqType;
import com.me.learn.rpc.contants.RpcConstant;
import com.me.learn.rpc.contants.SerialType;
import com.me.learn.rpc.core.Header;
import com.me.learn.rpc.core.RpcProtocol;
import com.me.learn.rpc.core.RpcRequest;
import com.me.learn.rpc.core.RpcResponse;
import com.me.learn.rpc.serial.ISerializer;
import com.me.learn.rpc.serial.SerializerManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.me.learn.rpc.contants.ReqType.findByCode;

/**
 * 将字节流转化为RpcProtocol
 *
 * @Author: Jed
 * Created: 2021/9/16
 **/
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        log.info("==================Begin RpcDecoder========================");
        if (in.readableBytes() < RpcConstant.HEAD_TOTOAL_LEN) {
            return;
        }

        in.markReaderIndex();
        //魔数 2字节
        final short magic = in.readShort();
        if (RpcConstant.MAGIC != magic) {
            throw new IllegalArgumentException("Illegal Request parameter 'magic' {}" + magic);
        }
        final byte serialType = in.readByte();
        final byte reqType = in.readByte();
        final long requestId = in.readLong();
        final int dataLength = in.readInt();

        //verification
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        //
        Header header = new Header(magic, serialType, reqType, requestId, dataLength);

        //找到对应的序列化类型
        ISerializer serializer = SerializerManager.getSerializer(serialType);
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);
        ReqType requestType = findByCode(reqType);
        switch (requestType) {
            case REQUEST:

                RpcRequest rpcRequest = serializer.deserialize(data, RpcRequest.class);

                if (SerialType.JSON_SERIAL.code() == serialType) {
                    if (rpcRequest.getParameterTypes() != null && rpcRequest.getParameterTypes().length > 0) {
                        Object[] parameters = new Object[rpcRequest.getParameterTypes().length];
                        for (int i = 0; i < rpcRequest.getParameterTypes().length; i++) {
                            parameters[i] = JSON.parseObject(rpcRequest.getParas()[i].toString(), rpcRequest.getParameterTypes()[i]);
                        }
                        rpcRequest.setParas(parameters);
                    }
                }

                RpcProtocol<RpcRequest> rpcProtocol = new RpcProtocol<>();
                rpcProtocol.setHeader(header);
                rpcProtocol.setContent(rpcRequest);
                out.add(rpcProtocol);
                break;
            case RESPONSE:
                RpcResponse response = serializer.deserialize(data, RpcResponse.class);
                RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
                resProtocol.setHeader(header);
                resProtocol.setContent(response);
                out.add(resProtocol);
                break;
            case HEARTBEAT:
                //TODO
                break;
            default:
                break;
        }
    }
}
