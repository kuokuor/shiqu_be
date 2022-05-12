package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.CollectService;
import com.kuokuor.shiqu.service.FollowService;
import com.kuokuor.shiqu.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

/**
 * 用户Controller
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 17:28
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private CollectService collectService;

    /**
     * 登录
     *
     * @param email      邮箱
     * @param password   密码
     * @param rememberMe 记住我
     * @return
     */
    @PostMapping("/login")
    public R login(String email, String password, boolean rememberMe) {
        String message = userService.login(email, password);

        //如果map中只存在用户ID一个数据，说明是成功登录
        if (message.startsWith("ID:")) {
            //转换为用户ID
            int userId = Integer.parseInt(message.substring(3));
            //将用户登录状态进行改变
            //选择记住我则登录状态保持一周, 否则仅本次有效
            if (rememberMe) {
                StpUtil.login(userId, new SaLoginModel().setTimeout(Constants.REMEMBER_ME));
            } else {
                StpUtil.login(userId, false);
            }
            return R.ok();
        } else {
            //否则就是登录失败
            return R.fail(message);
        }
    }

    /**
     * 登出
     *
     * @return
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    /**
     * 发送验证码For注册
     *
     * @param email 邮箱
     * @return
     */
    @PostMapping("/sendCodeForRegister")
    public R sendCodeForRegister(String email) {
        String msg = userService.sendCodeForRegister(email);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 注册
     *
     * @param email    邮箱
     * @param password 密码
     * @param code     验证码
     * @return
     */
    @PostMapping("/register")
    public R register(String email, String password, String code) {
        String msg = userService.register(email, password, code);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 发送验证码For重置密码
     *
     * @param email 邮箱
     * @return
     */
    @PostMapping("/sendCodeForResetPass")
    public R sendCodeForResetPass(String email) {
        String msg = userService.sendCodeForResetPass(email);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 找回密码
     *
     * @param email    邮箱
     * @param password 密码
     * @param code     验证码
     * @return
     */
    @PostMapping("/resetPass")
    public R resetPass(String email, String password, String code) {
        String msg = userService.resetPass(email, password, code);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 查询用户信息[用于用户详情页]
     *
     * @param userId
     * @return
     */
    @PostMapping("/getUserInfo")
    public R getUserInfo(int userId) {
        Object data = userService.getUserInfoForUserPage(userId,
                StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null);
        return data == null ? R.fail("该用户不存在") : R.ok(data);
    }

    @SaCheckLogin
    @PostMapping("/updateUserInfo")
    public R updateUserInfo(User user) {
        if (user == null) return R.fail("未获取到用户信息!");
        // 基础处理
        if (user.getNickname().length() > 15) return R.fail("昵称过长!");
        if (user.getSex() < 0 || user.getSex() > 2) return R.fail("请选择正确的性别类型!");
        // 将nickname Description
        user.setNickname(HtmlUtils.htmlEscape(user.getNickname()));
        user.setDescription(HtmlUtils.htmlEscape(user.getDescription()));
        // 设置用户编号
        user.setId(StpUtil.getLoginIdAsInt());
        String msg = userService.updateUser(user);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 得到当前用户信息
     *
     * @return
     */
    @SaCheckLogin   // 已登录才能访问该方法
    @GetMapping("/getHolderInfo")
    public R getHolderInfo() {
        return R.ok(userService.getHolderInfo(StpUtil.getLoginIdAsInt()));
    }

    /**
     * 查询当前用户Id[不存在则返回空]
     *
     * @return
     */
    @GetMapping("/getHolderUserId")
    public R getHolderUserId() {
        return R.ok(StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null);
    }

    @PostMapping("/searchByNickname")
    public R searchByNickname(String key) {
        if (StringUtils.isBlank(key)) {
            return R.fail("键为空!");
        }
        return R.ok(userService.searchByNickname(StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null, key));
    }

    // ----------------------------以上为用户信息相关操作----------------------------

    /**
     * 关注 取消关注
     *
     * @param userId
     * @return
     */
    @SaCheckLogin
    @PostMapping("/changeFollowed")
    public R changeFollowed(int userId) {
        if (!userService.exitsUser(userId)) {
            return R.fail("用户不存在!");
        }
        int holderId = StpUtil.getLoginIdAsInt();
        // 是否以及关注
        boolean hasFollow = followService.hasFollowed(holderId, Constants.ENTITY_TYPE_USER, userId);
        if (hasFollow) {
            // 取消关注
            followService.unfollow(holderId, Constants.ENTITY_TYPE_USER, userId);
        } else {
            // 关注
            // 防止用户关注自己
            if (holderId == userId) {
                return R.fail("不能关注自己!");
            }
            followService.follow(holderId, Constants.ENTITY_TYPE_USER, userId, userId);
        }
        return R.ok();
    }

    /**
     * 获取用户关注其他用户的列表
     *
     * @param userId
     * @return
     */
    @PostMapping("/getFollowList")
    public R getFollowList(int userId) {
        // 看当前有没有用户登录[登录就给用户Id赋值]
        Integer holderId = StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
        // 不管是用户不存在还是未关注任何用户都会返回null
        List<Map<String, Object>> followeeInfo = followService.queryFolloweeList(holderId, userId, 0, Integer.MAX_VALUE);
        // 将数据返回[没有数据则为空]
        return R.ok(followeeInfo);
    }

    /**
     * 获取用户的粉丝列表
     *
     * @param userId
     * @return
     */
    @PostMapping("/getFansList")
    public R getFansList(int userId) {
        // 看当前有没有用户登录[登录就给用户Id赋值]
        Integer holderId = StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
        // 不管是用户不存在还是未关注任何用户都会返回null
        List<Map<String, Object>> fansInfo = followService.queryFansList(holderId, userId, 0, Integer.MAX_VALUE);
        // 将数据返回[没有数据则为空]
        return R.ok(fansInfo);
    }

    /**
     * 获取用户收藏的帖子列表
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @PostMapping("/getCollectedNoteList")
    public R getCollectedNoteList(int userId, int offset, int limit) {
        List<Map<String, Object>> collectedPostList = collectService.getCollectedPostList(userId, offset, limit);
        return R.ok(collectedPostList);
    }

    // ----------------------------以上为关注相关操作----------------------------

}
