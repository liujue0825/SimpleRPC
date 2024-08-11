package com.lj.rpc.server.handler;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.RpcResponse;
import com.lj.rpc.core.constant.ProtocolConstants;
import com.lj.rpc.core.enums.MessageStatus;
import com.lj.rpc.core.enums.MessageType;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.factory.RateLimiterFactory;
import com.lj.rpc.core.factory.ThreadPoolFactory;
import com.lj.rpc.core.protocol.MessageHeader;
import com.lj.rpc.core.protocol.RpcMessage;
import com.lj.rpc.core.ratelimit.RateLimit;
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
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Rpc 请求消息处理器
 * <p>
 * 只关心客户端发送的请求消息, 因此继承 SimpleChannelInboundHandler , 泛型设置为 RpcMessage
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 15:39
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private final ThreadPoolExecutor threadPool;

    private final RateLimiterFactory rateLimiterFactory;

    public RpcRequestHandler() {
        // 使用定义好的线程工厂类来创建线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool();
        rateLimiterFactory = new RateLimiterFactory(100, 10, 1000);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        threadPool.submit(() -> {
            try {
                RpcMessage response = new RpcMessage();
                MessageHeader header = msg.getMessageHeader();
                MessageType messageType = MessageType.parseType(header.getMessageType());
                log.debug("The message received by the server is: {}", msg.getBody());
                // 处理 RPC 请求
                if (messageType == MessageType.REQUEST) {
                    // 处理 RPC 请求之前进行限流判断
                    RpcRequest request = (RpcRequest) msg.getBody();
                    String serviceName = request.getServiceName();
                    RpcResponse rpcResponse = new RpcResponse();

                    // RateLimit rateLimiter = rateLimiterFactory.getRateLimiter(serviceName);
                    // NOTE: 触发限流, 快速返回
                    // if (!rateLimiter.isRelease()) {
                    //     rpcResponse.setExceptionValue(new RpcException("rate limit!"));
                    //     header.setMessageStatus(MessageStatus.FAIL.getStatus());
                    //     response.setMessageHeader(header);
                    //     ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    // }
                    header.setMessageType(MessageType.RESPONSE.getType());
                    // 反射调用
                    try {
                        // 获取本地反射调用结果
                        Object result = getRequest(request);
                        rpcResponse.setReturnValue(result);
                        header.setMessageStatus(MessageStatus.SUCCESS.getStatus());
                    } catch (Exception e) {
                        log.error("The service [{}], the method [{}] invoke failed!",
                                request.getServiceName(), request.getMethod());
                        // 若不设置，堆栈信息过多，导致报错
                        rpcResponse.setExceptionValue(
                                new RpcException("Error in remote procedure call, " + e.getMessage()));
                        header.setMessageStatus(MessageStatus.FAIL.getStatus());
                    }
                    response.setMessageHeader(header);
                    response.setBody(rpcResponse);
                } else if (messageType == MessageType.HEARTBEAT_REQUEST) {    // 如果是心跳检测请求
                    header.setMessageType(MessageType.HEARTBEAT_RESPONSE.getType());
                    header.setMessageStatus(MessageStatus.SUCCESS.getStatus());
                    response.setMessageHeader(header);
                    response.setBody(ProtocolConstants.HEARTBEAT_RESPONSE);
                }
                log.debug("responseRpcMessage: {}.", response);
                // 将结果进行传递
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } finally {
                // 确保 ByteBuf 被释放，防止发生内存泄露
                ReferenceCountUtil.release(msg);
            }
        });
    }

    /**
     * 通过反射调用 RpcRequest 请求指定的方法
     *
     * @param request RPC 请求
     * @return 调用结果
     */
    private Object getRequest(RpcRequest request)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
     * 处理读空闲: 服务端当检测到读空闲, 直接关闭客户端的 channel 连接
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
                log.debug("idle check happened, so now close the connection!");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
