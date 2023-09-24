package com.hc.hcapiinterface.controller;


import com.hc.hcapicommon.model.entity.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @创建人 Alexshi
 * @创建时间 2023/8/1
 * @描述
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(String name) {
        return "Get 你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "Post 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
//        String accessKey = request.getHeader("accessKey");
//        String nonce = request.getHeader("nonce");
//        String timestamp = request.getHeader("timestamp");
//        String sign = request.getHeader("sign");
//        String body = request.getHeader("body");
//
//
//        //待修改
//        if(!accessKey.equals("hc")){
//            throw new RuntimeException("accessKey无权限");
//        }
//
//        if(Long.parseLong(nonce)>10000){
//            throw new RuntimeException("nonce权限");
//        }
//        //todo 时间和当前时间不能超过5分钟
////        if(timestamp){
////
////        }
//
//        //待修改，从数据库中取出secretKey
//        String serverSign = SignUtils.getSign(body,"1234abcd");
//         if(!sign.equals(serverSign)){
//            throw new RuntimeException("secret无权限");
//        }
        return "Post 用户名字是" + user.getUserName();
    }
}
