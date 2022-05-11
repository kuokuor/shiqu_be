package com.kuokuor.shiqu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.commom.domain.R;
import com.kuokuor.shiqu.entity.Comment;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
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

    @Autowired
    private CommentService commentService;

    /**
     * 查询帖子列表
     *
     * @param index  0表示关注，1表示热门，2表示最新
     * @param limit
     * @param offset
     * @param userId 为0时查询所有用户
     * @return
     */
    @PostMapping("/getNoteList")
    public R getNoteList(int index, int limit, int offset, Integer userId) {
        if (index == 0) {
            // 如果当前用户未登录就返回空
            if (!StpUtil.isLogin()) {
                return R.ok(new ArrayList<>());
            }
            return R.ok(noteService.queryFolloweeNotes(StpUtil.getLoginIdAsInt(), offset, limit));
        } else {
            return R.ok(noteService.queryAllByLimit(userId, offset, limit, index - 1));
        }
    }

    /**
     * 获取笔记详情
     *
     * @param noteId
     * @return
     */
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

    /**
     * 新增笔记
     *
     * @param title
     * @param content
     * @param type
     * @param tags
     * @param photoList
     * @return
     */
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
     * 查询待编辑的笔记信息
     *
     * @param noteId
     * @return
     */
    @SaCheckLogin
    @GetMapping("/getNoteForUpdate/{noteId}")
    public R getNoteForUpdate(@PathVariable("noteId") int noteId) {
        Map<String, Object> data = noteService.getPostForUpdate(StpUtil.getLoginIdAsInt(), noteId);
        return data == null ? R.fail("获取笔记信息出错!") : R.ok(data);
    }

    /**
     * 修改笔记
     *
     * @param id
     * @param title
     * @param content
     * @param type
     * @param tags
     * @param photoList
     * @return
     */
    @SaCheckLogin
    @PostMapping("/updatePost")
    public R updatePost(Integer id, String title, String content, int type, int[] tags, String[] photoList) {
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || type < 0 || type > 1 || photoList.length == 0) {
            return R.fail("请检查各项数据!");
        }
        Note note = new Note();
        note.setId(id);
        // 当前登录的用户ID就是帖子作者 后面再判断对不对
        Integer userId = StpUtil.getLoginIdAsInt();
        note.setUserId(userId);
        note.setTitle(title);
        note.setContent(content);
        note.setType(type);
        note.setHeadImg(photoList[0]);

        String msg = noteService.updatePost(note, tags, photoList);
        return msg == null ? R.ok("修改成功!") : R.fail(msg);
    }

    /**
     * 点赞[取消点赞也是调用该方法, 在业务层进行判断]
     *
     * @param entityType   被点赞的实体类型
     * @param entityId     被点赞的实体ID
     * @param entityUserId 被点赞的实体的发布者
     * @param noteId       帖子Id, 让前端传入, 减少数据库交互次数
     * @return
     */
    @SaCheckLogin
    @PostMapping("/changeLiked")
    public R changeLiked(int entityType, int entityId, int entityUserId, int noteId) {
        int userId = StpUtil.getLoginIdAsInt();
        String msg = likeService.like(userId, entityType, entityId, entityUserId, noteId);
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
     * @param type    指定类型 0全部 1美食 2探店
     * @param current 当前页[从0开始]
     * @param limit   记录数
     * @return
     */
    @PostMapping("/search")
    public R search(String keyword, Integer type, int current, int limit) {
        type--;
        if (type == -1) type = null;
        return R.ok(searchService.searchPostList(keyword, current, limit, type));
    }

    /**
     * 发送评论
     *
     * @param comment
     * @return
     */
    @SaCheckLogin
    @PostMapping("/sendComment")
    public R sendComment(Comment comment) {
        comment.setUserId(StpUtil.getLoginIdAsInt());
        // 进行Html转义
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        String msg = commentService.addComment(comment);
        return msg == null ? R.ok() : R.fail(msg);
    }

    @SaCheckLogin
    @PostMapping("/deleteComment")
    public R deleteComment(int commentId) {
        String msg = commentService.deleteComment(commentId, StpUtil.getLoginIdAsInt());
        return msg == null ? R.ok() : R.fail(msg);
    }

    /**
     * 查询指定tag下的所有笔记
     *
     * @param tag
     * @return
     */
    @PostMapping("/classify")
    public R classify(int tag) {
        return R.ok(noteService.classify(tag));
    }

}
