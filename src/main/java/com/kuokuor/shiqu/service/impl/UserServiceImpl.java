package com.kuokuor.shiqu.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.event.Event;
import com.kuokuor.shiqu.event.EventProducer;
import com.kuokuor.shiqu.service.RedisService;
import com.kuokuor.shiqu.service.UserService;
import com.kuokuor.shiqu.utils.PasswordUtil;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisService redisService;

    @Autowired
    private EventProducer eventProducer;

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

    /**
     * 发送验证码For注册
     *
     * @param email
     * @return
     */
    @Override
    public String sendCodeForRegister(String email) {
        // 验证邮箱
        User u = userDao.queryByEmail(email);
        if (u != null) {
            return "邮箱已被注册!";
        }

        // 6位验证码
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        // 将验证码放到Redis里存起来(5分钟过期)
        redisService.setCacheObject(RedisKeyUtil.getRegisterCodeKey(email), code, Constants.FIND_PASSWORD_CODE_EXPIRATION, TimeUnit.MINUTES);
        // 发送邮件通知用户
        String text = "验证码为: " + code + "<br/>亲爱的用户, 您正在注册食趣.  该验证码5分钟内有效, 请尽快完成操作." +
                "<br/><br/><br/>" +
                "<div style=\"font-size:60%; color:#b1b3b8\">该邮件由系统自动发出。<br/>" +
                "若您未进行相关操作, 请忽略本邮件, 对您造成打扰, 非常抱歉!</div>";

        // 发布事件发送邮件
        Event event = new Event()
                .setTopic(Constants.TOPIC_SEND_MAIL)
                .setData("to", email)
                .setData("subject", "注册食趣")
                .setData("content", text);
        // 发布
        eventProducer.fireEvent(event);

        return null;
    }

    /**
     * 发送验证码For重置密码
     *
     * @param email
     * @return
     */
    @Override
    public String sendCodeForResetPass(String email) {
        User user = userDao.queryByEmail(email);
        if (user == null)
            return "用户不存在!";

        // 存在该用户则给用户发送邮件
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        // 将验证码放到Redis里存起来(5分钟过期)
        redisService.setCacheObject(RedisKeyUtil.getFindPasswordCodeKey(email), code, Constants.FIND_PASSWORD_CODE_EXPIRATION, TimeUnit.MINUTES);
        // 发送邮件通知用户
        String text = "验证码为: " + code + "<br/>您正在找回密码, 该验证码5分钟内有效, 请尽快完成操作." +
                "<br/><br/><br/>" +
                "<div style=\"font-size:60%; color:#b1b3b8\">该邮件由系统自动发出。<br/>" +
                "若您未进行相关操作, 请忽略本邮件, 对您造成打扰, 非常抱歉!</div>";

        // 发布事件发送邮件
        Event event = new Event()
                .setTopic(Constants.TOPIC_SEND_MAIL)
                .setData("to", email)
                .setData("subject", "找回密码")
                .setData("content", text);
        // 发布
        eventProducer.fireEvent(event);

        return null;
    }

    /**
     * 注册
     *
     * @param email
     * @param password
     * @param code
     * @return
     */
    @Override
    public String register(String email, String password, String code) {
        //空值处理
        if (StringUtils.isBlank(email)) {
            return "邮箱为空!";
        }
        if (StringUtils.isBlank(password)) {
            return "密码为空!";
        }
        if (StringUtils.isBlank(code)) {
            return "验证码为空!";
        }

        // 验证验证码
        String redisKey = RedisKeyUtil.getRegisterCodeKey(email);
        String codeInRedis = redisService.getCacheObject(redisKey).toString();
        // 如果Redis不存在该账号的验证码或者验证码对不上
        if (codeInRedis == null || !codeInRedis.equals(code)) {
            return "验证码错误!";
        }

        // 验证账号
        if (userDao.queryByEmail(email) != null) {
            return "该邮箱已被注册!";
        }

        // 补充其他信息, 进行注册操作
        User user = new User();
        user.setEmail(email);
        // 设置盐值
        String salt = PasswordUtil.generateUUID().substring(0, 5);
        user.setSalt(salt);
        // 对密码进行加密
        user.setPassword(PasswordUtil.md5(password + salt));

        // 给定随机头像
        user.setAvatar(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 昵称
        user.setNickname(email);
        user.setSex(0);
        user.setDescription("Hello World!");
        // 普通用户
        user.setType(0);
        user.setCreateTime(new Date());
        //插入数据库
        userDao.insert(user);

        return null;
    }

    /**
     * 找回密码
     *
     * @param email
     * @param password
     * @param code
     * @return
     */
    @Override
    public String resetPass(String email, String password, String code) {
        String redisKey = RedisKeyUtil.getFindPasswordCodeKey(email);
        // 先判断存不存在, 防止空指针
        if (!redisService.hasKey(redisKey)) {
            return "验证码错误!";
        }
        String codeInRedis = redisService.getCacheObject(redisKey).toString();
        // 如果Redis不存在该账号的验证码或者验证码对不上
        if (codeInRedis == null || !codeInRedis.equals(code)) {
            return "验证码错误!";
        }
        // 否则就进行修改密码的操作
        User user = userDao.queryByEmail(email);
        // 防止操作过程中用户被删除
        if (user == null)
            return "用户不存在!";
        // 加密用户密码并修改
        user.setPassword(PasswordUtil.md5(password + user.getSalt()));
        userDao.update(user);
        // 在Redis里清除验证码
        redisService.deleteObject(redisKey);
        return null;
    }
}
