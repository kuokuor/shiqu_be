package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.NoteService;
import com.kuokuor.shiqu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台控制层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/23 16:39
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

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
        String message = userService.adminLogin(email, password);

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
     * 获取所有用户数据
     *
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/getAllUser")
    public R getAllUser() {
        return R.ok(userService.getAllUser());
    }

    /**
     * 获取所有笔记数据
     *
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/getAllNote")
    public R getAllNote() {
        return R.ok(noteService.getAllNote());
    }

    /**
     * 修改用户权限
     *
     * @param userId
     * @param isAdmin true-设置为管理员 false-取消管理员权限
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/grant")
    public R grant(int userId, boolean isAdmin) {
        String msg = userService.grant(userId, isAdmin ? 999 : 0);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/deleteUser")
    public R deleteUser(int userId) {
        // 类型为886代表删除
        String msg = userService.grant(userId, 886);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 删除帖子
     *
     * @param noteId
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/deleteNote")
    public R deleteNote(int noteId) {
        String msg = noteService.deleteNoteForAdmin(noteId);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 封禁用户
     *
     * @param userId
     * @param banTime 封禁时长，单位：秒  (86400秒=1天，此值为-1时，代表永久封禁)
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/banUser")
    public R banUser(int userId, long banTime) {
        // 先踢下线
        StpUtil.kickout(userId);
        StpUtil.disable(userId, banTime);
        return R.ok();
    }

    /**
     * 获取用户数量
     *
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/userCount")
    public R userCount() {
        return R.ok(userService.userCount());
    }

    /**
     * 获取笔记数量
     *
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/noteCount")
    public R noteCount() {
        return R.ok(noteService.noteCount());
    }

    /**
     * 是否被封禁
     *
     * @param userId
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/hasBan")
    public R hasBan(int userId) {
        return R.ok(StpUtil.isDisable(userId));
    }

    /**
     * 解封
     *
     * @param userId
     * @return
     */
    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/unBanUser")
    public R unBanUser(int userId) {
        StpUtil.untieDisable(userId);
        return R.ok();
    }

}
