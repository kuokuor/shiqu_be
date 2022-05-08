package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

/**
 * 用户Controller
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 17:28
 */
@CrossOrigin
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

    /**
     * 查询用户信息[用于用户详情页]
     *
     * @param userId
     * @return
     */
    @GetMapping("/getUserInfo")
    public R getUserInfo(int userId) {
        Object data = userService.getUserInfoForUserPage(userId,
                StpUtil.isLogin() == true ? StpUtil.getLoginIdAsInt() : null);
        return data == null ? R.fail("该用户不存在") : R.ok(data);
    }

    @SaCheckLogin
    @PostMapping("/updateUserInfo")
    public R updateUserInfo(User user) {
        if (user == null) return R.fail("未获取到用户信息!");
        // 基础处理
        if (user.getNickname().length() > 15) return R.fail("昵称过长!");
        if (user.getSex() < 0 || user.getSex() > 2) return R.fail("请选择正确的性别类型!");
        // 将nickname Description
        user.setNickname(HtmlUtils.htmlEscape(user.getNickname()));
        user.setDescription(HtmlUtils.htmlEscape(user.getDescription()));
        // 设置用户编号
        user.setId(StpUtil.getLoginIdAsInt());
        String msg = userService.updateUser(user);
        return msg == null ? R.ok() : R.fail(msg);
    }

}
