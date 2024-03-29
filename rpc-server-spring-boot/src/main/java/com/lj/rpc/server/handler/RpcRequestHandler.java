package com.lj.rpc.server.handler;

import com.lj.rpc.core.common.RpcRequest;
import com.lj.rpc.core.common.RpcResponse;
import com.lj.rpc.core.enums.MessageStatus;
import com.lj.rpc.core.enums.MessageType;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.protocol.MessageHeader;
import com.lj.rpc.core.protocol.RpcMessage;
import com.lj.rpc.core.registry.LocalRegisterCache;
import com.lj.rpc.server.cache.LocalServiceCache;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Rpc 请求消息处理器
 * <p>
 * 只关心客户端端发送的请求消息, 因此继承 SimpleChannelInboundHandler , 泛型设置为 RpcMessage
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 15:39
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private static final ThreadPoolExecutor THREAD_POOL =
            new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        THREAD_POOL.submit(() -> {
            try {
                RpcMessage response = new RpcMessage();
                MessageHeader header = msg.getMessageHeader();
                MessageType messageType = MessageType.parseType(header.getMessageType());
                log.debug("The message received by the server is: {}", msg.getBody());
                // 处理 RPC 请求
                if (messageType == MessageType.REQUEST) {
                    RpcRequest request = (RpcRequest) msg.getBody();
                    RpcResponse rpcResponse = new RpcResponse();
                    header.setMessageType(MessageType.RESPONSE.getType());
                    try {
                        // 反射调用
                        Object result = getRequest(request);
                        rpcResponse.setReturnValue(result);
                        header.setMessageStatus(MessageStatus.SUCCESS.getStatus());
                    } catch (Exception e) {
                        log.error("The service [{}], the method [{}] invoke failed!", request.getServiceName(), request.getMethod());
                        // 若不设置，堆栈信息过多，导致报错
                        rpcResponse.setExceptionValue(new RpcException("Error in remote procedure call, " + e.getMessage()));
                        header.setMessageStatus(MessageStatus.FAIL.getStatus());
                    }
                    response.setMessageHeader(header);
                    response.setBody(rpcResponse);
                }
                log.debug("responseRpcMessage: {}.", response);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        });
    }

    /**
     * 通过反射得到调用结果
     *
     * @param request RPC 请求
     * @return 调用结果
     */
    private Object getRequest(RpcRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String serviceName = request.getServiceName();
        // 根据 serviceName 去注册中心(本地缓存)拿
        Object service = LocalServiceCache.getService(serviceName);
        if (service == null) {
            log.error("The service [{}] is not exist!", request.getServiceName());
            throw new RpcException(String.format("The service [%s] is not exist!", request.getServiceName()));
        }
        Method method = service.getClass().getMethod(request.getMethod(), request.getParameterTypes());
        return method.invoke(service, request.getParameterValues());
    }

    /**
     * 用户自定义事件处理器
     * 处理读空闲: 服务端当检测到读空闲, 直接关闭 channel 连接
     *
     * @param ctx ctx
     * @param evt evt
     * @throws Exception exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 服务端检测到读空闲, 就直接断开与客户端的连接
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                log.debug("idle check happen, so close the connection.");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
