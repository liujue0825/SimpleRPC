package com.lj.rpc.core.factory;

import com.lj.rpc.core.enums.SerializerType;
import com.lj.rpc.core.serialization.Serialization;
import com.lj.rpc.core.serialization.hessian.HessianSerialization;
import com.lj.rpc.core.serialization.jdk.JdkSerialization;
import com.lj.rpc.core.serialization.json.JsonSerialization;
import com.lj.rpc.core.serialization.kryo.KryoSerialization;
import com.lj.rpc.core.serialization.protostuff.ProtoStuffSerialization;

/**
 * 序列化算法工厂，通过序列化算法枚举类型获取相应的序列化算法实例
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 17:10
 */
public final class SerializationFactory {

    private SerializationFactory() {

    }

    /**
     * 根据序列化类型字段得到对应的序列化算法
     *
     * @param serializerType 序列化算法类型
     * @return 对应的序列化算法实现类
     */
    public static Serialization getSerialization(SerializerType serializerType) {
        switch (serializerType) {
            case JDK:
                return new JdkSerialization();
            case JSON:
                return new JsonSerialization();
            case HESSIAN:
                return new HessianSerialization();
            case KRYO:
                return new KryoSerialization();
            case PROTOSTUFF:
                return new ProtoStuffSerialization();
            default:
                throw new IllegalArgumentException(
                        String.format("The serialization type %s is illegal.", serializerType.name()));
        }
    }
}
