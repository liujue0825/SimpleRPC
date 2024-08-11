package com.lj.rpc.core.extension;

/**
 * Holder 类，作用是为不可变的对象引用提供一个可变的包装
 *
 * @author liujue
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
