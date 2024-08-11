package com.lj.rpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * RPC 服务端相关参数类
 *
 * @author liujue
 */
@Data
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServerProperties {

    /**
     * 服务端 IP 地址, 默认值无需显示配置
     */
    private String address;

    /**
     * 服务端端口号, 默认值为: 8080
     */
    private Integer port;

    /**
     * 服务端名称, 默认值为: provider-0
     */
    private String appName;

    /**
     * 网络传输方式, 默认值为: netty
     */
    private String transport;

    /**
     * 服务注册/发现中心, 可选: zookeeper, nacos, 默认值为: zookeeper
     */
    private String registry;

    /**
     * 服务注册/发现中心地址, 默认值为: 127.0.0.1:2181
     */
    private String registryAddr;

    public RpcServerProperties() throws UnknownHostException {
        this.address = InetAddress.getLocalHost().getHostAddress();
        this.port = 8080;
        this.appName = "provide-0";
        this.transport = "netty";
        this.registry = "zookeeper";
        this.registryAddr = "127.0.0.1:2181";
    }
}
