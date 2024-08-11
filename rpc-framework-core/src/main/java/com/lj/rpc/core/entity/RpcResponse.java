package com.lj.rpc.core.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC 响应消息实体类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 17:37
 */
@Data
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 2024L;

    /**
     * 请求返回值
     */
    private Object returnValue;

    /**
     * 异常信息
     */
    private Exception exceptionValue;
}
