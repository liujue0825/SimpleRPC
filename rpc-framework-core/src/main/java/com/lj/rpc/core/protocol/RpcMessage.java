package com.lj.rpc.core.protocol;

import lombok.Data;

/**
 * 消息实体类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 15:07
 */
@Data
public class RpcMessage {

    /**
     * 自定义的协议请求头
     */
    private MessageHeader messageHeader;

    /**
     * 请求体
     */
    private Object body;
}
