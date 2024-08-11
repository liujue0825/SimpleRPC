package com.lj.rpc.core.exception;

/**
 * 序列化过程中产生的异常
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 14:34
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SerializationException() {
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
