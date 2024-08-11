package com.lj.rpc.core.constant;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义协议相关的常量
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 16:32
 */
public class ProtocolConstants {

    private ProtocolConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 魔数
     */
    public static final byte[] MAGIC_NUMS = new byte[]{(byte) 2, (byte) 0, (byte) 2, (byte) 4};

    /**
     * 版本号
     */
    public static final byte VERSION = 1;

    /**
     * 消息 id
     */
    private static final AtomicInteger MESSAGE_ID = new AtomicInteger();

    /**
     * 心跳检测请求消息内容
     */
    public static final String HEARTBEAT_REQUEST = "PING";

    /**
     * 心跳检测响应消息内容
     */
    public static final String HEARTBEAT_RESPONSE = "PONG";

    /**
     * 得到唯一的消息 id
     *
     * @return 唯一消息 id
     */
    public static int getSequenceId() {
        return MESSAGE_ID.getAndIncrement();
    }
}
