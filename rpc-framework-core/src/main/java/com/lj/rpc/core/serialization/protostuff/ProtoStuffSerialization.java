package com.lj.rpc.core.serialization.protostuff;

import com.lj.rpc.core.exception.SerializationException;
import com.lj.rpc.core.serialization.Serialization;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 ProtoStuff 库实现的 Java 序列化算法
 *
 * <p>
 *     出于灵活性考虑, 使用 ProtoStuff 代替 Protobuf 实现序列化算法
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/28 23:21
 */
public class ProtoStuffSerialization implements Serialization {

    /**
     * 提前分配好 Buffer，避免每次序列化都需要重新申请 Buffer 空间
     */
    private final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    /**
     * 缓存 Schema 实例，避免每次序列化都通过反射获取 Schema，提高性能
     */
    private static final ConcurrentHashMap<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T object) {
        Class<T> clazz = (Class<T>) object.getClass();
        Schema<T> schema = (Schema<T>) schemaCache.computeIfAbsent(clazz, RuntimeSchema::getSchema);
        try {
            return ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } catch (Exception e) {
            throw new SerializationException("Protostuff serialize failed.", e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T object = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, object, schema);
            return object;
        } catch (Exception e) {
            throw new SerializationException("Protostuff deserialize failed.", e);
        }
    }
}
