/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.serial;

import com.me.learn.rpc.contants.SerialType;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.*;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/16
 **/
public class JavaSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream bos = null;
        try {
            bos = new ObjectOutputStream(baos);
            bos.writeObject(object);
            bos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public byte getType() {
        return SerialType.JAVA_SERIAL.code();
    }
}
