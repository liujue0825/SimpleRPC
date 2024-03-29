package com.lj.rpc;

import com.lj.rpc.api.service.HelloService;

/**
 * @author liujue
 * @version 1.0
 * @description: TODO
 * @date 2024/1/26 14:24
 */
public class Consumer {
    public static void main(String[] args) {
        // TODO: RPC
        HelloService helloService = null;
        String msg = helloService.sayHello("liujue");
        System.out.println(msg);
    }
}
