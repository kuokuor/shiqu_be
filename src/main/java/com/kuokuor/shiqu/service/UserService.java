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
     * 通过Id查询用户
     *
     * @param userId
     * @return
     */
    User selectUserById(int userId);

}
