package com.lj.rpc.client.handler;

import com.lj.rpc.core.entity.RpcResponse;
import com.lj.rpc.core.constant.ProtocolConstants;
import com.lj.rpc.core.enums.MessageType;
import com.lj.rpc.core.enums.SerializerType;
import com.lj.rpc.core.protocol.MessageHeader;
import com.lj.rpc.core.protocol.RpcMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 响应消息处理器
 *
 * <p>
 * 只关心服务器端发送的响应消息, 因此继承 SimpleChannelInboundHandler , 泛型设置为 RpcMessage
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 14:57
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcMessage> {

    /**
     * 存放未处理的响应请求
     */
    public static final Map<Integer, Promise<RpcMessage>> UNPROCESSED_RPC_RESPONSES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        try {
            MessageType messageType = MessageType.parseType(msg.getMessageHeader().getMessageType());
            // 如果是 RPC 响应消息
            if (messageType == MessageType.RESPONSE) {
                int sequenceId = msg.getMessageHeader().getSequenceId();
                // 拿到还未执行完成的 promise 对象
                Promise<RpcMessage> promise = UNPROCESSED_RPC_RESPONSES.remove(sequenceId);
                if (promise != null) {
                    RpcResponse response = (RpcResponse) msg.getBody();
                    Exception exceptionValue = response.getExceptionValue();
                    if (exceptionValue == null) {
                        promise.setSuccess(msg);
                    } else {
                        promise.setFailure(exceptionValue);
                    }
                }
            } else if (messageType == MessageType.HEARTBEAT_RESPONSE) {  // 如果是心跳检查响应
                log.debug("Heartbeat info {}.", msg.getBody());
            }
        } finally {
            // 释放内存，防止内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 用户自定义事件处理器
     * 处理写空闲: 客户端定时心跳, 当检测到写空闲立即发送一个心跳检测数据包
     *
     * @param ctx ctx
     * @param evt evt
     * @throws Exception exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 发现写空闲, 则发送一个心跳检测数据包
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                // 构造心跳检测数据包, body 部分为心跳常量
                RpcMessage heartbeatMessage = new RpcMessage();
                MessageHeader header = MessageHeader.build(SerializerType.HESSIAN.name());
                header.setMessageType(MessageType.HEARTBEAT_REQUEST.getType());
                heartbeatMessage.setMessageHeader(header);
                heartbeatMessage.setBody(ProtocolConstants.HEARTBEAT_REQUEST);

                // 向服务器端发送数据
                ctx.writeAndFlush(heartbeatMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
