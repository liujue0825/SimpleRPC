package com.lj.rpc.client.transport.netty;

import com.lj.rpc.client.entity.RequestMetaData;
import com.lj.rpc.client.handler.RpcResponseHandler;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.codec.RpcFrameDecoder;
import com.lj.rpc.core.codec.SharableRpcMessageCodec;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.factory.SingletonFactory;
import com.lj.rpc.core.protocol.RpcMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于 Netty 框架实现的 RpcClient 客户端类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 22:17
 */
@Slf4j
public class NettyRpcClient implements RpcClient {

    /**
     * 启动器
     */
    private final Bootstrap bootstrap;

    /**
     * channel 对象缓存
     */
    private final ChannelCache channelCache;

    public NettyRpcClient() {
        this.bootstrap = new Bootstrap();
        // 事件循环组, 用于处理 channel 上的 io 事件
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 1. 添加 IdleStateHandler 解决连接假死问题
                        // 15s 内没有向服务器写数据, 会触发一个 IdleState#WRITER_IDLE 事件
                        ch.pipeline().addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
                        // 2. 添加 RpcFrameDecoder 解决黏包半包问题
                        ch.pipeline().addLast(new RpcFrameDecoder());
                        // 3. 添加 SharableRpcMessageCodec 对自定义协议进行编解码
                        ch.pipeline().addLast(new SharableRpcMessageCodec());
                        // 4. 添加 RpcResponseHandler 处理服务端发送的响应消息
                        ch.pipeline().addLast(new RpcResponseHandler());
                    }
                });
        // 初始化 channel
        this.channelCache = SingletonFactory.getInstance(ChannelCache.class);
    }

    @SneakyThrows
    @Override
    public RpcMessage sendRequest(RequestMetaData request) {
        // 1. 构建接收返回结果的 promise
        Promise<RpcMessage> promise;
        Channel channel = getChannel(new InetSocketAddress(request.getServerAddr(), request.getPort()));
        if (!channel.isActive()) {
            throw new IllegalStateException("The channel is inactivate.");
        }
        // 创建 promise 来接受结果, 其中参数为执行完成通知的线程
        promise = new DefaultPromise<>(channel.eventLoop());
        int sequenceId = request.getRpcMessage().getMessageHeader().getSequenceId();
        // 存入还未处理的请求
        RpcResponseHandler.UNPROCESSED_RPC_RESPONSES.put(sequenceId, promise);
        // 2. 发送数据并监听发送状态
        channel.writeAndFlush(request.getRpcMessage()).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("The client send the message successfully, msg: [{}].", request);
            } else {
                future.channel().close();
                promise.setFailure(future.cause());
                log.error("The client send the message failed.", future.cause());
            }
        });

        // 3. 等待结果返回--让出 CPU 资源，同步阻塞调用线程 main，其他线程 eventLoop 去执行获取操作
        Integer timeout = request.getTimeout();
        // 如果没有指定超时时间，则 await 直到 promise 完成
        if (timeout == null || timeout <= 0) {
            promise.await();
        } else {
            // 在指定超时时间内等待结果返回
            boolean success = promise.await(timeout, TimeUnit.MILLISECONDS);
            if (!success) {
                promise.setFailure(new TimeoutException(String.format("The Remote procedure call exceeded the " +
                        "specified timeout of %dms.", timeout)));
            }
        }
        if (promise.isSuccess()) {
            // 调用正常
            return promise.getNow();
        } else {
            // 调用失败
            throw new RpcException(promise.cause());
        }
    }

    /**
     * 建立连接得到元 channel 对象
     *
     * @param inetSocketAddress 服务端 socket 地址
     * @return channel 对象
     */
    public Channel doConnect(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("The client has successfully connected to server [{}]!", inetSocketAddress.toString());
                channelCompletableFuture.complete(future.channel());
            } else {
                throw new RpcException(String.format("The client failed to connect to [%s].", inetSocketAddress.toString()));
            }
        });
        Channel channel = channelCompletableFuture.get();
        channel.closeFuture().addListener(future ->
                log.info("The client has been disconnected from server [{}].", inetSocketAddress.toString()));
        return channel;
    }

    /**
     * 获取 channel 对象
     *
     * @param inetSocketAddress socket 地址
     * @return channel 对象
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        // 优先在缓存中找
        Channel channel = channelCache.get(inetSocketAddress);
        if (channel == null) {
            try {
                channel = doConnect(inetSocketAddress);
            } catch (ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error(e.getMessage(), e);
            }
            channelCache.set(inetSocketAddress, channel);
        }
        return channel;
    }
}
