package com.kuokuor.shiqu;

import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
