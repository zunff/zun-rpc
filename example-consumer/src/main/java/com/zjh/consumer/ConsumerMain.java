package com.zjh.consumer;

import com.zjh.common.model.User;
import com.zjh.common.service.UserService;

public class ConsumerMain {
    public static void main(String[] args) {
        //todo 获取UserServiceImpl示例
        UserService userService = null;

        User user = userService.getUser("ZunF");
        if (user == null) {
            System.out.println("user == null");
        } else {
            System.out.println(user);
        }

    }
}
