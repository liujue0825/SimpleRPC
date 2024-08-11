package com.lj.rpc.core.enums;

import lombok.Getter;

/**
 * 不同序列化方式的枚举类
 *
 * @author liujue
 * @date 2024/01/26
 */
@Getter
public enum SerializerType {

    /**
     * JDK 序列化算法
     */
    JDK((byte) 0),

    /**
     * JSON 序列化算法
     */
    JSON((byte) 1),

    /**
     * HESSIAN 序列化算法
     */
    HESSIAN((byte) 2),

    /**
     * KRYO 序列化算法
     */
    KRYO((byte) 3),

    /**
     * PROTOSTUFF 序列化算法
     */
    PROTOSTUFF((byte) 4);

    /**
     * 类型
     */
    private final byte type;

    SerializerType(byte type) {
        this.type = type;
    }

    /**
     * 根据协议字段的序列化类型 (byte) 得到对应的序列化算法枚举类 (SerializerType)
     *
     * @param type 序列化类型
     * @return 序列化算法
     */
    public static SerializerType parseType(byte type) {
        for (SerializerType serializerType : SerializerType.values()) {
            if (serializerType.getType() == type) {
                return serializerType;
            }
        }
        return HESSIAN;
    }

    /**
     * 根据指定的序列化名称 (String) 得到对应的序列化算法枚举类 (SerializerType)
     *
     * @param serializeName 指定的序列化名称
     * @return 序列化算法枚举类
     */
    public static SerializerType parseName(String serializeName) {
        for (SerializerType serializationType : SerializerType.values()) {
            if (serializationType.name().equalsIgnoreCase(serializeName)) {
                return serializationType;
            }
        }
        return HESSIAN;
    }
}
