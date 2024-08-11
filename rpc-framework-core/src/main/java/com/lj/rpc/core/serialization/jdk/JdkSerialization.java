package com.lj.rpc.core.serialization.jdk;


import com.lj.rpc.core.exception.SerializationException;
import com.lj.rpc.core.serialization.Serialization;

import java.io.*;

/**
 * 基于 JDK 实现的 Java 序列化算法
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 15:31
 */
public class JdkSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ObjectOutputStream(out).writeObject(object);
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Jdk serialize failed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = in.readObject();
            return (T) object;
        } catch (Exception e) {
            throw new SerializationException("Jdk deserialize failed.", e);
        }
    }
}
