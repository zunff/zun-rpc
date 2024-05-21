package com.zjh.consumer;

import com.zjh.common.model.User;
import com.zjh.common.service.UserService;
import com.zjh.rpc.proxy.ServiceProxyFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 消费者
 *
 * @author zunf
 * @date 2024/5/6 01:02
 */
public class ConsumerMain {
    public static void main(String[] args) {

        //获取UserServiceImpl示例
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = userService.getUser("ZunF1");
        if (user == null) {
            System.out.println("user == null");
        } else {
            System.out.println(user);
        }

        user = userService.getUser("ZunF2");
        if (user == null) {
            System.out.println("user == null");
        } else {
            System.out.println(user);
        }

        user = userService.getUser("ZunF3");
        if (user == null) {
            System.out.println("user == null");
        } else {
            System.out.println(user);
        }
    }
}
