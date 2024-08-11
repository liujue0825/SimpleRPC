package com.lj.rpc.core.extension;

/**
 *
 * @author liujue
 */
@SPI
public interface ExtensionFactory {

    /**
     * 获取扩展对象
     *
     * @param type 对象类型
     * @param name 对象名
     * @param <T>  实例类型
     * @return 实例对象
     */
    <T> T getExtension(Class<T> type, String name);
}
