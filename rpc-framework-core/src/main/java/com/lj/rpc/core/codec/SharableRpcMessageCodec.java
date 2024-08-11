package com.lj.rpc.core.codec;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.RpcResponse;
import com.lj.rpc.core.constant.ProtocolConstants;
import com.lj.rpc.core.enums.MessageType;
import com.lj.rpc.core.enums.SerializerType;
import com.lj.rpc.core.protocol.MessageHeader;
import com.lj.rpc.core.protocol.RpcMessage;
import com.lj.rpc.core.serialization.Serialization;
import com.lj.rpc.core.factory.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义的消息编解码器
 *
 * <p>必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 *
 * <p>可共享, 无需保存 ByteBuf 的状态信息
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 16:08
 */
@ChannelHandler.Sharable
public class SharableRpcMessageCodec extends MessageToMessageCodec<ByteBuf, RpcMessage> {

    /**
     * 将 RpcMessage 对象编码为 ByteBuf 对象
     * <p>出站处理, 构造协议头
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, List<Object> list) {
        ByteBuf buf = ctx.alloc().buffer();
        MessageHeader header = rpcMessage.getMessageHeader();
        // 4 字节 魔数
        buf.writeBytes(header.getMagicNum());
        // 1 字节 版本号
        buf.writeByte(header.getVersion());
        // 1 字节 序列化方式
        buf.writeByte(header.getSerializerType());
        // 1 字节 消息类型
        buf.writeByte(header.getMessageType());
        // 1 字节 状态类型
        buf.writeByte(header.getMessageStatus());
        // 4 字节 消息序列号
        buf.writeInt(header.getSequenceId());

        // 根据 body 长度计算正文长度
        Object body = rpcMessage.getBody();
        // 根据序列化方式将正文部分序列化成字节数组
        Serialization serialization =
                SerializationFactory.getSerialization(SerializerType.parseType(header.getSerializerType()));
        byte[] bytes = serialization.serialize(body);
        header.setBodyLength(bytes.length);

        // 4 字节 正文长度
        buf.writeInt(header.getBodyLength());

        // 不固定字节 消息体部分
        buf.writeBytes(bytes);

        // 传递给下一个出站处理器
        list.add(buf);
    }

    /**
     * 将 ByteBuf 对象编码为 RPCMessage 对象
     * <p>入站处理, 对相关字段进行校验
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        int len = ProtocolConstants.MAGIC_NUMS.length;
        byte[] magicNum = new byte[len];
        // 魔数校验
        byteBuf.readBytes(magicNum, 0, len);
        for (int i = 0; i < len; i++) {
            if (magicNum[i] != ProtocolConstants.MAGIC_NUMS[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(magicNum));
            }
        }
        // 版本号校验
        byte version = byteBuf.readByte();
        if (version != ProtocolConstants.VERSION) {
            throw new IllegalArgumentException("The version isn't compatible " + version);
        }
        // 1 字节 序列化方式
        byte serializerType = byteBuf.readByte();
        // 1 字节 消息类型
        byte messageType = byteBuf.readByte();
        // 1 字节 状态类型
        byte messageStatus = byteBuf.readByte();
        // 4 字节 消息序列号
        int sequenceId = byteBuf.readInt();
        // 4 字节 正文长度
        int bodyLength = byteBuf.readInt();
        // 正文部分
        byte[] bytes = new byte[bodyLength];
        byteBuf.readBytes(bytes, 0, bodyLength);

        // 组合上述信息, 构造协议头对象 MessageHeader
        MessageHeader header = MessageHeader.builder()
                .magicNum(magicNum)
                .version(version)
                .serializerType(serializerType)
                .messageType(messageType)
                .messageStatus(messageStatus)
                .sequenceId(sequenceId)
                .bodyLength(bodyLength)
                .build();

        // 获取序列化算法
        Serialization serialization =
                SerializationFactory.getSerialization(SerializerType.parseType(serializerType));

        // 构造 RpcMessage 对象
        RpcMessage msg = new RpcMessage();
        msg.setMessageHeader(header);

        // 根据不同的消息类型进行反序列化
        MessageType type = MessageType.parseType(messageType);
        if (type == MessageType.REQUEST) {
            RpcRequest body = serialization.deserialize(RpcRequest.class, bytes);
            msg.setBody(body);
        } else if (type == MessageType.RESPONSE) {
            RpcResponse body = serialization.deserialize(RpcResponse.class, bytes);
            msg.setBody(body);
        }

        // 传递给下一个入站处理器
        list.add(msg);
    }
}

