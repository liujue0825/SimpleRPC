package com.lj.rpc.core.enums;

import lombok.Getter;

/**
 * RPC 通信过程中的不同消息类型的枚举类
 *
 * @author liujue
 * @date 2024/01/26
 */
@Getter
public enum MessageType {

    /**
     * 类型 0 表示请求消息
     */
    REQUEST((byte) 0),

    /**
     * 类型 1 表示响应消息
     */
    RESPONSE((byte) 1),

    /**
     * 类型 2 表示心跳检测请求消息
     */
    HEARTBEAT_REQUEST((byte) 2),

    /**
     * 类型 3 表示心跳检测响应消息
     */
    HEARTBEAT_RESPONSE((byte) 3);

    private final byte type;

    MessageType(byte type) {
        this.type = type;
    }

    /**
     * 根据消息类型 (byte) 获取消息枚举类 (MessageType)
     *
     * @param type type
     * @return {@link MessageType}
     */
    public static MessageType parseType(byte type) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getType() == type) {
                return messageType;
            }
        }
        throw new IllegalArgumentException(String.format("The message type %s is illegal.", type));
    }
}
