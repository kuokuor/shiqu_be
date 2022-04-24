package com.kuokuor.shiqu.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.UserService;
import com.kuokuor.shiqu.utils.PasswordUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 16:33
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    /**
     * 登录
     *
     * @param email      邮箱[作为账号]
     * @param password   密码
     * @param rememberMe 记住我
     * @return
     */
    @Override
    public String login(String email, String password, boolean rememberMe) {

        //空值处理
        if (StringUtils.isBlank(email)) {
            return "邮箱为空!";
        }
        if (StringUtils.isBlank(password)) {
            return "密码为空!";
        }

        User user = userDao.queryByEmail(email);

        if (user == null) {
            return "用户不存在!";
        }

        // 判断账号是否注销[886为注销]
        if (user.getType() == Constants.USER_TYPE_DESTROY) {
            return "账号已被注销!";
        }

        //将提供的密码进行相同方式的加密
        password = PasswordUtil.md5(password + user.getSalt());

        //验证密码
        if (!password.equals(user.getPassword())) {
            return "密码错误!";
        }

        // 正常登录
        if (rememberMe) {
            // 记住我
            StpUtil.login(user.getId(), new SaLoginModel().setTimeout(Constants.REMEMBER_ME));
        } else {
            // 不记住我[关闭浏览器则退出登录]
            StpUtil.login(user.getId(), false);
        }
        return null;
    }

    /**
     * 得到当前用户信息
     *
     * @param userId 用户Id
     * @return
     */
    @Override
    public User getHolderInfo(int userId) {
        return userDao.queryById(userId);
    }
}
