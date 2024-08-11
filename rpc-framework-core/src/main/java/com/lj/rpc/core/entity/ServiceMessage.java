package com.lj.rpc.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务注册信息实体类, 封装了服务提供方向注册中心提交的信息
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 11:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMessage {

    /**
     * 服务端名称
     */
    private String appName;

    /**
     * 服务名称: 服务名 + 版本号
     */
    private String serviceName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 服务提供方的主机地址
     */
    private String inetAddress;

    /**
     * 服务提供方的端口号
     */
    private Integer port;

    /**
     * 服务幂等性情况
     */
    private boolean isIdempotent;
}
