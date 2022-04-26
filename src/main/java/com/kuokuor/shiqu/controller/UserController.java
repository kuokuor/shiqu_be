package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 登录
     *
     * @param email      邮箱
     * @param password   密码
     * @param rememberMe 记住我
     * @return
     */
    @PostMapping("/login")
    public R login(String email, String password, boolean rememberMe) {
        String msg = userService.login(email, password, rememberMe);
        return msg == null ? R.ok("成功登录") : R.fail(msg);
    }

    /**
     * 得到当前用户信息
     *
     * @return
     */
    @SaCheckLogin   // 已登录才能访问该方法
    @GetMapping("/getHolderInfo")
    public R getHolderInfo() {
        return R.ok(userService.getHolderInfo(StpUtil.getLoginIdAsInt()));
    }

    /**
     * 登出
     *
     * @return
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    /**
     * 发送验证码For注册
     *
     * @param email 邮箱
     * @return
     */
    @PostMapping("/sendCodeForRegister")
    public R sendCodeForRegister(String email) {
        String msg = userService.sendCodeForRegister(email);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 注册
     *
     * @param email    邮箱
     * @param password 密码
     * @param code     验证码
     * @return
     */
    @PostMapping("/register")
    public R register(String email, String password, String code) {
        String msg = userService.register(email, password, code);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 发送验证码For重置密码
     *
     * @param email 邮箱
     * @return
     */
    @PostMapping("/sendCodeForResetPass")
    public R sendCodeForResetPass(String email) {
        String msg = userService.sendCodeForResetPass(email);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 找回密码
     *
     * @param email    邮箱
     * @param password 密码
     * @param code     验证码
     * @return
     */
    @PostMapping("/resetPass")
    public R resetPass(String email, String password, String code) {
        String msg = userService.resetPass(email, password, code);
        return msg == null ? R.ok() : R.fail(msg);
    }

}
