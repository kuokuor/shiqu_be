package com.kuokuor.shiqu.controller;

import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户Controller
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 17:28
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R login(String email, String password, boolean rememberMe) {
        String msg = userService.login(email, password, rememberMe);
        return msg == null ? R.ok("成功登录") : R.fail(msg);
    }

}
