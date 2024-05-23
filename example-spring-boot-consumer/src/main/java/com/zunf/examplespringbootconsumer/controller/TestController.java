package com.zunf.examplespringbootconsumer.controller;

import com.zjh.common.model.User;
import com.zjh.common.service.UserService;
import com.zunf.rpc.annotation.ZunRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @ZunRpcReference
    private UserService userService;

    @GetMapping("/")
    public String test() {
        User zunf = userService.getUser("zunf");
        return zunf.toString();
    }

}
