package com.kuokuor.shiqu.controller;

import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/24 19:03
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

}
