package com.kuokuor.shiqu.service;

import com.kuokuor.shiqu.entity.User;

/**
 * 用户业务层接口
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 16:33
 */
public interface UserService {

    /**
     * 登录
     *
     * @param email      邮箱[作为账号]
     * @param password   密码
     * @param rememberMe 记住我
     * @return
     */
    String login(String email, String password, boolean rememberMe);

    /**
     * 得到当前用户信息
     *
     * @param userId 用户Id
     * @return
     */
    User getHolderInfo(int userId);

    /**
     * 发送验证码For注册
     *
     * @param email
     * @return
     */
    String sendCodeForRegister(String email);

    /**
     * 发送验证码For重置密码
     *
     * @param email
     * @return
     */
    String sendCodeForResetPass(String email);

    /**
     * 注册
     *
     * @param email
     * @param password
     * @param code
     * @return
     */
    String register(String email, String password, String code);

    /**
     * 找回密码
     *
     * @param email
     * @param password
     * @param code
     * @return
     */
    String resetPass(String email, String password, String code);
}
