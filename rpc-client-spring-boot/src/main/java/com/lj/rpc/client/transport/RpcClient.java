package com.lj.rpc.client.transport;

import com.lj.rpc.client.entity.RequestMetaData;
import com.lj.rpc.core.protocol.RpcMessage;

/**
 * Rpc 客户端类，负责向服务端发起请求
 *
 * @author liujue
 * @date 2024/01/26
 */
public interface RpcClient {

    /**
     * 发起远程过程调用
     *
     * @param request RPC 请求内容
     * @return RPC 响应内容
     */
    RpcMessage sendRequest(RequestMetaData request);
}
