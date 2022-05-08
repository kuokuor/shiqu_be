package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.service.FollowService;
import com.kuokuor.shiqu.service.LikeService;
import com.kuokuor.shiqu.service.NoteService;
import com.kuokuor.shiqu.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.Map;

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

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private SearchService searchService;

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

    @GetMapping("/getNoteDetail/{noteId}")
    public R getNoteDetail(@PathVariable("noteId") int noteId) {
        // 得到当前用户的ID
        Integer userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsInt();
        }

        Map<String, Object> postDetail = noteService.queryNoteDetailById(noteId, userId);
        if (postDetail == null) {
            R.fail("查询帖子信息出现错误!");
        }

        // 如果当前用户已登录则将用户的ID也返回, 用于前端处理
        return userId == null ? R.ok(postDetail) : R.ok(postDetail, userId.toString());
    }

    @SaCheckLogin
    @PostMapping("/createNote")
    public R createNote(String title, String content, int type, int[] tags, String[] photoList) {
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || type < 0 || type > 1 || photoList.length == 0) {
            return R.fail("请检查各项数据!");
        }
        Note note = new Note();
        // 进行HTML转义, 防止用户恶意攻击
        note.setTitle(HtmlUtils.htmlEscape(title));
        note.setContent(HtmlUtils.htmlEscape(content));
        note.setType(type);
        note.setUserId(StpUtil.getLoginIdAsInt());
        note.setCreateTime(new Date());
        note.setHeadImg(photoList[0]);
        note.setScore(0D);

        String msg = noteService.insertNote(note, photoList, tags);
        return msg == null ? R.ok("发布成功!") : R.fail(msg);
    }

    /**
     * 删除帖子
     *
     * @param noteId
     * @return
     */
    @SaCheckLogin
    @PostMapping("/deleteNote")
    public R deletePost(int noteId) {
        String msg = noteService.deleteNote(StpUtil.getLoginIdAsInt(), noteId);
        return msg == null ? R.ok() : R.fail(msg);
    }


    /**
     * 点赞[取消点赞也是调用该方法, 在业务层进行判断]
     *
     * @param entityType   被点赞的实体类型
     * @param entityId     被点赞的实体ID
     * @param entityUserId 被点赞的实体的发布者
     * @param postId       帖子Id, 让前端传入, 减少数据库交互次数
     * @return
     */
    @SaCheckLogin
    @PostMapping("/changeLiked")
    public R changeLiked(int entityType, int entityId, int entityUserId, int postId) {
        int userId = StpUtil.getLoginIdAsInt();
        String msg = likeService.like(userId, entityType, entityId, entityUserId, postId);
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 收藏帖子 取消收藏
     *
     * @param noteId
     * @return
     */
    @SaCheckLogin
    @PostMapping("/changeCollected")
    public R changeCollected(int noteId) {
        Note note = noteService.getNoteById(noteId);
        if (note == null) {
            return R.fail("帖子不存在!");
        }
        int holderId = StpUtil.getLoginIdAsInt();
        // 是否已经收藏
        boolean hasCollect = followService.hasFollowed(holderId, Constants.ENTITY_TYPE_NOTE, noteId);
        if (hasCollect) {
            // 取消收藏
            followService.unfollow(holderId, Constants.ENTITY_TYPE_NOTE, noteId);
        } else {
            // 收藏
            followService.follow(holderId, Constants.ENTITY_TYPE_NOTE, noteId, note.getUserId());
        }
        return R.ok();
    }

    /**
     * 搜索
     *
     * @param keyword 关键字
     * @param type    指定类型
     * @param current 当前页[从0开始]
     * @param limit   记录数
     * @return
     */
    @GetMapping("/search")
    public R search(String keyword, Integer type, int current, int limit) {
        return R.ok(searchService.searchPostList(keyword, current, limit, type));
    }

}
