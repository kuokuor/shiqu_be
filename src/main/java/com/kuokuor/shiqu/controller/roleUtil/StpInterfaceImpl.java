package com.kuokuor.shiqu.controller.roleUtil;

import cn.dev33.satoken.stp.StpInterface;
import com.kuokuor.shiqu.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/26 19:24
 */
@Component    // 保证此类被SpringBoot扫描，完成Sa-Token的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserDao userDao;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return null;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        int type = userDao.getUserType(Integer.valueOf(loginId.toString()));
        List<String> list = new ArrayList<String>();
        if (type == 999) {
            // 管理员
            list.add("admin");
        }
        return list;
    }

}
