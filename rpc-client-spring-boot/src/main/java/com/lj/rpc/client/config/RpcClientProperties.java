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
     * 负载均衡算法, 包括: random, roundRobin, consistentHash, 其中默认值为 roundRobin
     */
    private String loadBalance;

    /**
     * 序列化算法, 包括: JDK, JSON, HESSIAN, KRYO, PROTOSTUFF, 其中默认值为 HESSIAN
     */
    private String serialization;

    /**
     * 网络传输方式, 包括: netty, 其中默认值为 netty
     */
    private String transport;

    /**
     * 服务注册/发现中心, 包括: zookeeper, nacos, 其中默认值为 zookeeper
     */
    private String registry;

    /**
     * 服务注册/发现中心地址, 默认值为: 127.0.0.1:2181
     */
    private String registryAddr;

    /**
     * 连接超时时间, 单位毫秒, 默认值为 5000
     */
    private Integer timeout;

    public RpcClientProperties() {
        this.loadBalance = "roundRobin";
        this.serialization = "hessian";
        this.transport = "netty";
        this.registry = "zookeeper";
        this.registryAddr = "127.0.0.1:2181";
        this.timeout = 5000;
    }
}
