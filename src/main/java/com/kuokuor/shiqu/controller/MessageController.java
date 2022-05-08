package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * 消息控制层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/7 17:14
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送私信
     *
     * @param toId
     * @param content
     * @return
     */
    @SaCheckLogin
    @PostMapping("/sendText")
    public R sendLetter(int toId, String content) {
        int holderUserId = StpUtil.getLoginIdAsInt();
        // html转义
        content = HtmlUtils.htmlEscape(content);
        String msg = messageService.sendLetter(holderUserId, toId, content);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 获取聊天记录
     *
     * @param targetId 对方编号
     * @return
     */
    @SaCheckLogin
    @GetMapping("/getChatList")
    public R getChatList(int targetId) {
        int holderUserId = StpUtil.getLoginIdAsInt();
        // 组合出conversationId
        String conversationId = holderUserId < targetId ?
                holderUserId + "_" + targetId : targetId + "_" + holderUserId;
        // 把当前用户的Id放在msg里, 让前端分辨
        return R.ok(messageService.getLetterDetail(holderUserId, conversationId), holderUserId + "");
    }

    /**
     * 获取私信列表
     *
     * @return
     */
    @SaCheckLogin
    @GetMapping("/getLetterList")
    public R getLetterList() {
        return R.ok(messageService.getLetterHome(StpUtil.getLoginIdAsInt()));
    }

}
