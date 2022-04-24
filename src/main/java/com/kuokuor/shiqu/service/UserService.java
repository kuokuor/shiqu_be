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
     * 获取验证码
     *
     * @param email
     * @return
     */
    String getVerificationCode(String email);
}
