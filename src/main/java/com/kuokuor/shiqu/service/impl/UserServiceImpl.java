package com.kuokuor.shiqu.service.impl;

import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.UserService;
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
     * 通过Id查询用户
     *
     * @param userId
     * @return
     */
    @Override
    public User selectUserById(int userId) {
        return userDao.queryById(userId);
    }
}
