package com.lj.rpc.consumer;

import com.lj.rpc.api.service.HelloService;
import com.lj.rpc.client.annotation.RpcReference;

/**
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 14:24
 */

public class Consumer {

    @RpcReference
    private static HelloService helloService;

    public static void main(String[] args) {
        // NOTE: 基于 RPC 实现远程调用就可以得到返回结果
        String msg = helloService.sayHello("liujue");
        System.out.println(msg);
    }
}
