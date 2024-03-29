package com.lj.rpc.client.transport.netty;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存 Channel 对象, 以达到 channel 复用的目的
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/31 14:59
 */
public class ChannelCache {

    /**
     * Channel 对象集合, key: socket 地址; value: Channel 对象
     */
    private static final Map<InetSocketAddress, Channel> CHANNELS = new ConcurrentHashMap<>();

    /**
     * 获取 channel 对象
     *
     * @param inetSocketAddress 服务端 socket 地址
     * @return channel 对象
     */
    public Channel get(InetSocketAddress inetSocketAddress) {
        if (CHANNELS.containsKey(inetSocketAddress)) {
            Channel channel = CHANNELS.get(inetSocketAddress);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                CHANNELS.remove(inetSocketAddress);
            }
        }
        return null;
    }

    /**
     * 添加 channel 对象
     *
     * @param inetSocketAddress 服务端 socket 地址
     * @param channel           channel 对象
     */
    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        CHANNELS.put(inetSocketAddress, channel);
    }

}
