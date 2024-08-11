package com.lj.rpc.core.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 自定义的 RPC 帧解码器, 用于解决黏包半包问题
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 16:02
 * @see LengthFieldBasedFrameDecoder
 */
public class RpcFrameDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 根据自定义的协议设置的参数
     */
    public RpcFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    /**
     * RPC 帧解码器
     *
     * @param maxFrameLength      数据最大长度
     * @param lengthFieldOffset   长度字段偏移量，从第几个字节开始是内容的长度字段
     * @param lengthFieldLength   长度字段本身的长度
     * @param lengthAdjustment    长度字段为基准，几个字节后才是内容
     * @param initialBytesToStrip 从头开始剥离几个字节解码后显示
     */
    public RpcFrameDecoder(int maxFrameLength,
                           int lengthFieldOffset,
                           int lengthFieldLength,
                           int lengthAdjustment,
                           int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
