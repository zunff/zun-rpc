package com.zunf.examplespringbootprovider.service;

import com.zjh.common.model.User;
import com.zjh.common.service.UserService;
import com.zunf.rpc.annotation.ZunRpcService;

/**
 * UserService实例
 *
 * @author zunf
 */
@ZunRpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(String name) {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(i);
        }
        return new User(name, 38);
    }
}
