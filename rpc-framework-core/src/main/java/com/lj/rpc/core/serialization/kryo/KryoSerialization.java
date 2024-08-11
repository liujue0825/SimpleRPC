package com.lj.rpc.core.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.RpcResponse;
import com.lj.rpc.core.exception.SerializationException;
import com.lj.rpc.core.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 基于 Kryo 库实现 Java 序列化算法
 *
 * <p>
 * 注意: 除 Java 外，Scala 和 Kotlin 这些基于 JVM 的语言同样可以使用 Kryo 实现序列化
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/28 23:21
 */
public class KryoSerialization implements Serialization {

    /**
     * 因为 Kryo 不是线程安全的, 因此这里使用 ThreadLocal 来保存 Kryo 对象；
     * 也可以通过构造 Kryo 对象池的方式解决线程安全问题
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // object -> byte[]
            kryo.writeObject(output, object);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializationException("Kryo serialize failed.", e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte[] -> object
            T object = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return object;
        } catch (Exception e) {
            throw new SerializationException("Kryo deserialize failed.", e);
        }
    }
}
