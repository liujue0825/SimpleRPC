package com.lj.rpc.core.exception;

import java.io.Serializable;

/**
 * 远程过程调用时产生的异常
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 14:31
 */
public class RpcException extends RuntimeException {
    public static final long serialVersionUID = 1234567890123456789L;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
