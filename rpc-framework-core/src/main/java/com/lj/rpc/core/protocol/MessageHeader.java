package com.lj.rpc.core.protocol;

import com.lj.rpc.core.constant.ProtocolConstants;
import com.lj.rpc.core.enums.MessageType;
import com.lj.rpc.core.enums.SerializerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求协议头部信息:
 *
 * <pre>
 * -------------------------------------------------------------------------
 * | 魔数 (4byte) | 版本号 (1byte)  | 序列化方式 (1byte)  | 消息类型 (1byte) |
 * -------------------------------------------------------------------------
 * |  状态类型 (1byte)  |     消息序列号 (4byte)   |     正文长度 (4byte)    |
 * -------------------------------------------------------------------------
 * |                          消息主体 (不固定)                             |
 * -------------------------------------------------------------------------
 *  </pre>
 *
 * @author liujue
 * @version 1.0
 * @description: 自定义协议的请求头
 * @date 2024/1/26 15:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageHeader {

    /**
     * 魔数, 4 个字节
     */
    private byte[] magicNum;

    /**
     * 版本号, 1 个字节
     */
    private byte version;

    /**
     * 序列化方式, 1 个字节
     */
    private byte serializerType;

    /**
     * 消息类型, 1 个字节
     */
    private byte messageType;

    /**
     * 状态类型, 1 个字节
     */
    private byte messageStatus;

    /**
     * 消息序列号, 4 个字节
     */
    private int sequenceId;

    /**
     * 正文长度, 4 个字节
     */
    private int bodyLength;

    /**
     * 根据输入的序列化算法构造一个 MessageHeader 对象
     * 建造者模式
     *
     * @param serializeName 序列化算法名
     * @return 构造出的自定义协议的消息头
     */
    public static MessageHeader build(String serializeName) {
        return MessageHeader.builder()
                .magicNum(ProtocolConstants.MAGIC_NUMS)
                .version(ProtocolConstants.VERSION)
                // 协议状态在服务端处理时填入
                .serializerType(SerializerType.parseName(serializeName).getType())
                .messageType(MessageType.REQUEST.getType())
                .sequenceId(ProtocolConstants.getSequenceId())
                // 正文长度在编码时填入
                .build();
    }
}
