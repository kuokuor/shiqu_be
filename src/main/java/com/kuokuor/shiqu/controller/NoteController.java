package com.kuokuor.shiqu.controller;

import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 笔记控制层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 23:25
 */
@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    /**
     * 查询帖子列表
     *
     * @param index  0表示关注，1表示热门，2表示最新
     * @param limit
     * @param offset
     * @param userId 为空时查询所有用户
     * @return
     */
    @GetMapping("/getNoteList")
    public R getNoteList(int index, int limit, int offset, Integer userId) {
        if (index == 0) {
            // TODO: 查询关注的用户的帖子
            return R.ok();
        } else {
            return R.ok(noteService.queryAllByLimit(userId, offset, limit, index - 1));
        }
    }

}
