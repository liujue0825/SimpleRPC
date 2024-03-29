package com.lj.rpc.core.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务缓存, 将提供的服务实体类缓存到本地
 * <p>
 * TODO: 服务注册后需要拷贝一份到本地服务缓存中
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 16:01
 */
public class LocalRegisterCache {
    /**
     * 服务实例缓存, key: 服务名; value: 服务实体类
     */
    private static final Map<String , Object> SERVICE_MAP = new ConcurrentHashMap<>();

    public static Object getService(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }
    public static void register(String serviceName, Object obj) {
        SERVICE_MAP.put(serviceName, obj);
    }

    public static void removeService(String serviceName) {
        SERVICE_MAP.remove(serviceName);
    }
}
