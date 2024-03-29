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
     * 服务端 IP 地址
     */
    private String address;

    /**
     * 服务端端口号
     */
    private Integer port;

    /**
     * 服务端名称
     */
    private String appName;

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


    public RpcServerProperties() throws UnknownHostException {
        this.address = InetAddress.getLocalHost().getHostAddress();
        this.port = 8080;
        this.appName = "provide-0";
        this.transport = "netty";
        this.registry = "zookeeper";
        this.registryAddr = "192.168.101.129:2181";
    }
}
