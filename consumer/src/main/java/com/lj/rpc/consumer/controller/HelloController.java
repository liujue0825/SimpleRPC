package com.lj.rpc.consumer.controller;


import com.lj.rpc.api.service.HelloService;
import com.lj.rpc.client.annotation.RpcReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liujue
 * @version 1.0
 * @description: TODO
 * @date 2024/1/26 14:57
 */
@RestController
public class HelloController {
    @RpcReference
    private HelloService helloService;

    @RequestMapping("/hello/{name}")
    public String sayHello(@PathVariable String name) {
        return helloService.sayHello(name);
    }
}
