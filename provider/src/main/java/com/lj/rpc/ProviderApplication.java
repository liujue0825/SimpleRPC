package com.lj.rpc;

import com.lj.rpc.server.annotation.RpcComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liujue
 * @version 1.0
 * @description 服务端
 * @date 2024/3/28 20:00
 */
@RpcComponentScan(basePackages = {"com.lj.rpc.provider"})
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
