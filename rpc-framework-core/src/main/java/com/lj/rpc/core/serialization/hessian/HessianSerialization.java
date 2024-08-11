package com.lj.rpc.core.serialization.hessian;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import com.lj.rpc.core.exception.SerializationException;
import com.lj.rpc.core.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 基于 Hessian 库实现的 Java 序列化算法
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/28 23:14
 */
public class HessianSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            HessianSerializerOutput hso = new HessianSerializerOutput();
            hso.writeObject(object);
            hso.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Hessian serialize failed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            HessianSerializerInput hsi = new HessianSerializerInput(bis);
            return (T) hsi.readObject();
        } catch (Exception e) {
            throw new SerializationException("Hessian deserialize failed.", e);
        }
    }

}
