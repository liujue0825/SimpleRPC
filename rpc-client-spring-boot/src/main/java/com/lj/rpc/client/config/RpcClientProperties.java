package com.lj.rpc.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RPC 客户端相关参数类
 *
 * @author liujue
 */
@Data
@ConfigurationProperties(prefix = "rpc.client")
public class RpcClientProperties {
    /**
     * 负载均衡算法
     */
    private String loadBalance;

    /**
     * 序列化算法
     */
    private String serialization;

    /**
     * 网络传输方式
     */
    private String transport;

    /**
     * 服务注册/发现中心
     */
    private String registry;

    /**
     * 服务注册/发现中心地址
     */
    private String registryAddr;

    /**
     * 连接超时时间, 单位毫秒
     */
    private Integer timeout;

    public RpcClientProperties() {
        this.loadBalance = "roundRobin";
        this.serialization = "hessian";
        this.transport = "netty";
        this.registry = "zookeeper";
        this.registryAddr = "192.168.101.129:2181";
        this.timeout = 5000;
    }
}
