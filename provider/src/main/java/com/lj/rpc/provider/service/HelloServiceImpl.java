package com.lj.rpc.provider.service;

import com.lj.rpc.api.service.HelloService;

/**
 * @author liujue
 * @version 1.0
 * @description: TODO
 * @date 2024/1/26 14:21
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello: " + name;
    }
}
