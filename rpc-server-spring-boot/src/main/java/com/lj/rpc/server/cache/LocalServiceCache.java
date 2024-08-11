package com.lj.rpc.server.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务缓存, 将提供的服务实体类缓存到本地
 *
 * <p>
 * 服务注册后需要拷贝一份到本地服务缓存中
 * </p>
 *
 * @author liujue
 */
public class LocalServiceCache {

    /**
     * key: 服务名称(服务名 + 版本号), value: 服务注册信息实体类
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 添加到本地缓存
     *
     * @param serviceName 服务名称
     * @param object      服务注册信息实体类(ServiceMessage)
     */
    public static void addService(String serviceName, Object object) {
        SERVICE_MAP.put(serviceName, object);
    }

    /**
     * 获取服务注册信息实体类
     *
     * @param serviceName 服务名称
     * @return 服务注册信息实体类(ServiceMessage)
     */
    public static Object getService(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }

    /**
     * 将服务从本地缓存中删除
     *
     * @param serviceName 服务名称
     */
    public void removeService(String serviceName) {
        SERVICE_MAP.remove(serviceName);
    }
}
