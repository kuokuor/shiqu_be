package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.stp.StpUtil;

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

    @SaCheckLogin
    @RequestMapping("/getUser")
    public R getUserById(int id) {
        return R.ok(userService.selectUserById(id));
    }

    @SaCheckLogin
    @RequestMapping("/getHolderUserId")
    public R getHolderUserId() {
        return R.ok(StpUtil.getLoginIdAsInt());
    }

}
