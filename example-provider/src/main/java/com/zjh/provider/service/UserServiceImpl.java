package com.zjh.provider.service;

import com.zjh.common.model.User;
import com.zjh.common.service.UserService;

/**
 * UserService实现类
 *
 * @author zunf
 * @date 2024/5/7 11:08
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(String name) {
        return new User(name, 23);
    }
}
