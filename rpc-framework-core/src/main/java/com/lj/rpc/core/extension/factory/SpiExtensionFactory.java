package com.lj.rpc.core.extension.factory;

import com.lj.rpc.core.extension.ExtensionFactory;
import com.lj.rpc.core.extension.ExtensionLoader;
import com.lj.rpc.core.extension.SPI;

/**
 * @author liujue
 */
public class SpiExtensionFactory implements ExtensionFactory {

    /**
     * 获取扩展对象
     *
     * @param type 对象类型
     * @param name 对象名
     * @return 实例对象
     */
    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<?> extensionLoader = ExtensionLoader.getExtensionLoader(type);
            // TODO: implement this method
        }
        return null;
    }
}
