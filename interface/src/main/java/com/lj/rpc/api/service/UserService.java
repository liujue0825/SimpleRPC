package com.lj.rpc.api.service;

import com.lj.rpc.api.pojo.User;

/**
 * @author liujue
 * @version 1.0
 * @since 2024/7/18
 */
public class UserService {

    public boolean login(User user) {
        return user.getPassword().equals("123456");
    }
}
