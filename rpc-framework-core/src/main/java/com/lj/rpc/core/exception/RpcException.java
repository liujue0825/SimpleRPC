package com.lj.rpc.core.exception;


/**
 * 远程过程调用时产生的异常
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 14:31
 */
public class RpcException extends RuntimeException {

    private static final long serialVersionUID = 1L;

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
