package com.lj.rpc.provider.service;

import com.lj.rpc.api.pojo.User;
import com.lj.rpc.api.service.UserService;
import com.lj.rpc.server.annotation.RpcService;

/**
 * @author liujue
 * @version 1.0
 * @since 2024/7/18
 */
@RpcService(interfaceClass = UserService.class)
public class UserServiceImpl extends UserService {

    @Override
    public boolean login(User user) {
        System.out.println("login...");
        return super.login(user);
    }
}
