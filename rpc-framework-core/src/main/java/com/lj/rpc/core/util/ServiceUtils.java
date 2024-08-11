package com.lj.rpc.core.util;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lj.rpc.core.entity.ServiceMessage;

/**
 * 服务注册与发现相关的工具类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 22:58
 */
public class ServiceUtils {

    private ServiceUtils() {

    }

    /**
     * 根据 服务名称 + 版本号 生成注册服务的 key
     *
     * @param serverName 服务名(类的全限定名, clazz.getName())
     * @param version    版本号
     * @return 生成最终的服务名称: serverName-version
     */
    public static String getServiceName(String serverName, String version) {
        return String.join("-", serverName, version);
    }

    /**
     * 拆分出版本号
     *
     * @param serviceName 服务名称
     * @return 版本号
     */
    public static String split(String serviceName) {
        String[] parts = serviceName.split("-");
        return parts[1];
    }

    /**
     * 将 Instance 类型转换成 ServiceMessage
     *
     * @param instance 服务实例
     * @return 服务信息
     */
    public static ServiceMessage toServiceMessage(Instance instance) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setServiceName(instance.getServiceName());
        String version = ServiceUtils.split(instance.getServiceName());
        serviceMessage.setVersion(version);
        serviceMessage.setInetAddress(instance.getIp());
        serviceMessage.setPort(instance.getPort());
        return serviceMessage;
    }
}
