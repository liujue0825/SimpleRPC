package com.lj.rpc.provider.service;

import com.lj.rpc.api.service.HelloService;
import com.lj.rpc.server.annotation.RpcService;

/**
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 14:21
 */
@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello: " + name;
    }
}
