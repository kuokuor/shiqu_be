package com.kuokuor.shiqu.controller;

import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户Controller
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 17:28
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/getUser")
    public R getUserById(int id) {
        return R.ok(userService.selectUserById(id));
    }

}
