package com.lj.rpc.core.serialization.json;

import com.google.gson.*;
import com.lj.rpc.core.exception.SerializationException;
import com.lj.rpc.core.serialization.Serialization;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 基于 GSON 库实现的 JSON 序列化算法
 *
 * <p>
 *     解决了无法正确序列化 Class 类型的问题: java.lang.UnsupportedOperationException
 * </p>
 *
 * <p>
 *     解决方式: 手动实现一个针对 Class 的类型转换器
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/28 22:44
 */
public class JsonSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
            String json = gson.toJson(object);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Json serialize failed.", e);
        }

    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
            String json = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new SerializationException("Json deserialize failed.", e);
        }
    }

    /**
     * 自定义一个类型转换器用于序列化 Class 类型
     */
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @SneakyThrows
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            // json -> class
            String str = jsonElement.getAsString();
            return Class.forName(str);
        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            // class -> json
            return new JsonPrimitive(aClass.getName());
        }
    }
}
