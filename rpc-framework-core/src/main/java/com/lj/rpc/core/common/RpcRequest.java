package com.lj.rpc.core.common;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC 请求消息实体类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 17:37
 */
@Data
public class RpcRequest implements Serializable {
    /**
     * 服务名称：请求的服务名 + 版本
     */
    private String serviceName;

    /**
     * 请求调用的方法名称
     */
    private String method;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 参数
     */
    private Object[] parameterValues;
}
