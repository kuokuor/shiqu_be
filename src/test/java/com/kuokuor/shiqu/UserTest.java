package com.kuokuor.shiqu;

import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 用户测试类
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 16:36
 */
@SpringBootTest
public class UserTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Test
    public void search() {
        String key = "%Great%";
        List<User> userList = userDao.searchByNickname(key);
        for (User user : userList) {
            System.out.println(user.getNickname());
        }

    }

}
