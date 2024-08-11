package com.lj.rpc.server.transport.netty;

import com.lj.rpc.core.codec.RpcFrameDecoder;
import com.lj.rpc.core.codec.SharableRpcMessageCodec;
import com.lj.rpc.server.handler.RpcRequestHandler;
import com.lj.rpc.server.transport.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Netty 框架实现的 RpcServer 服务类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 14:41
 */
@Slf4j
public class NettyRpcServer implements RpcServer {

    @Override
    public void start(Integer port) {
        // boss 处理 ServerSocketChannel 上的 accept 事件
        EventLoopGroup boss = new NioEventLoopGroup();
        // worker 负责 SocketChannel 上的 read/write 事件
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    // TCP 默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 1. 添加 IdleStateHandler 解决连接假死问题
                            // 30s 内没有收到客户端发送的信息, 会触发一个 IdleState#READER_IDLE 事件
                            ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            // 2. 添加 RpcFrameDecoder 解决黏包半包问题
                            ch.pipeline().addLast(new RpcFrameDecoder());
                            // 3. 添加 SharableRpcMessageCodec 对自定义协议进行编解码
                            ch.pipeline().addLast(new SharableRpcMessageCodec());
                            // 4. 添加 RpcRequestHandler 处理客户端发送的请求消息
                            ch.pipeline().addLast(new RpcRequestHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(inetAddress, port).sync();
            log.debug("Rpc server add {} started on the port {}.", inetAddress, port);
            channelFuture.channel().closeFuture().sync();
        } catch (UnknownHostException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("An error occurred while starting the rpc service.", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
