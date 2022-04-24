package com.kuokuor.shiqu.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * 处理密码的工具类
 *
 * @Author: GreatBiscuit
 * @Date: 2022/2/19 15:57
 */
public class PasswordUtil {

    //生成随机字符
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    //MD5加密
    public static String md5(String password) {
        return StringUtils.isBlank(password) ? null : DigestUtils.md5DigestAsHex(password.getBytes());
    }

}
