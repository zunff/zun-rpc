package com.zjh.consumer;

import com.zjh.common.model.User;
import com.zjh.common.service.UserService;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.proxy.factory.ServiceProxyFactory;
import com.zjh.rpc.utils.ConfigUtils;

public class ConsumerMain {
    public static void main(String[] args) {

        //获取UserServiceImpl示例
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = userService.getUser("ZunF");
        if (user == null) {
            System.out.println("user == null");
        } else {
            System.out.println(user);
        }
    }
}
