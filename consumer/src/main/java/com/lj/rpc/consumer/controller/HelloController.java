package com.lj.rpc.consumer.controller;


import com.lj.rpc.api.pojo.User;
import com.lj.rpc.api.service.AbstractService;
import com.lj.rpc.api.service.HelloService;
import com.lj.rpc.client.annotation.RpcReference;
import com.lj.rpc.api.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liujue
 * @version 1.0
 * @date 2024/1/26 14:57
 */
@RestController
@RequestMapping
public class HelloController {

    @RpcReference
    private HelloService helloService;

    @RpcReference
    private AbstractService abstractService;

    @RpcReference
    private UserService userService;

    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return helloService.sayHello(name);
    }

    @RequestMapping("/hello/test/{count}")
    public Map<String, Long> performTest(@PathVariable("count") Long count) {
        Map<String, Long> result = new HashMap<>();
        result.put("调用次数", count);
        long start = System.currentTimeMillis();
        for (long i = 0; i < count; i++) {
            helloService.sayHello(Long.toString(i));
        }
        result.put("耗时", System.currentTimeMillis() - start);
        return result;
    }

    @RequestMapping("/abstracthello/{name}")
    public String abstractHello(@PathVariable("name") String name) {
        return abstractService.abstractHello(name);
    }

    @RequestMapping("/login/{name}")
    public String sayLogin(@PathVariable String name) {
        User user = new User(name, "123456", 18);
        boolean isLogin = userService.login(user);
        if (isLogin) {
            return "登录成功, 欢迎 " + name;
        } else {
            return "登录失败, 密码错误";
        }
    }
}
