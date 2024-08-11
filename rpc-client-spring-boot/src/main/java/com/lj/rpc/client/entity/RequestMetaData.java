package com.lj.rpc.client.entity;

import com.lj.rpc.core.protocol.RpcMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求元数据实体类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 14:24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMetaData {

    /**
     * 消息协议 - （请求头协议信息 + 请求信息）
     */
    private RpcMessage rpcMessage;

    /**
     * 远程服务提供方地址
     */
    private String serverAddr;

    /**
     * 远程服务提供方端口号
     */
    private Integer port;

    /**
     * 调用未响应的超时时间
     */
    private Integer timeout;
}
