package com.lj.rpc.core.enums;


import lombok.Getter;

/**
 * RPC 通信过程中不同消息状态的枚举类
 *
 * @author liujue
 * @date 2024/01/26
 */
@Getter
public enum MessageStatus {

    /**
     * 成功
     */
    SUCCESS((byte) 0),

    /**
     * 失败
     */
    FAIL((byte) 1);

    private final byte status;

    MessageStatus(byte status) {
        this.status = status;
    }

    /**
     * 根据协议的消息状态字段 (byte) 转换成对应的消息状态枚举类 (MessageStatus)
     *
     * @param status 自定义协议的消息状态字段
     * @return 消息状态枚举类
     */
    public static MessageStatus parseStatus(byte status) {
        for (MessageStatus messageStatus : MessageStatus.values()) {
            if (messageStatus.getStatus() == status) {
                return messageStatus;
            }
        }
        throw new IllegalArgumentException(String.format("The message status %s is illegal.", status));
    }
}
