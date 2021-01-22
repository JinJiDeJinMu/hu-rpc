package com.hjb.controller;

import com.hjb.proxy.RpcProxyFactory;
import com.hjb.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping
public class UserController {
    
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Object test(){

        System.out.println("消息发送成功");

        return null;
    }

    public static void main(String[] args) {
        UserService proxy = RpcProxyFactory.create(UserService.class);

        String hello = proxy.test("sss");
        System.out.println("消息返回结果" + hello);

    }

}
