package com.kuokuor.shiqu.service.impl;

import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.CommentDao;
import com.kuokuor.shiqu.dao.NoteDao;
import com.kuokuor.shiqu.entity.Comment;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.event.Event;
import com.kuokuor.shiqu.event.EventProducer;
import com.kuokuor.shiqu.service.CommentService;
import com.kuokuor.shiqu.service.RedisService;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 评论业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 20:34
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisService redisService;

    /**
     * 新增评论
     *
     * @param comment
     * @return
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public String addComment(Comment comment) {
        if (comment == null) {
            return "评论出错!";
        }
        comment.setState(0);
        comment.setCreateTime(new Date());

        int noteId = comment.getEntityId();
        // 如果当前是对评论的回复，则查询出评论所在的帖子ID
        if (comment.getEntityType() == Constants.ENTITY_TYPE_COMMENT) {
            Comment fatherComment = commentDao.queryById(comment.getEntityId());
            noteId = fatherComment.getEntityId();
        }

        // 插入数据库
        commentDao.insert(comment);

        // 触发系统通知, 使系统给用户发送消息
        Event commentEvent = new Event()
                .setTopic(Constants.TOPIC_COMMENT)
                .setUserId(comment.getUserId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("noteId", noteId)
                .setData("commentId", comment.getId());
        // 找出该消息是发给谁的
        if (comment.getEntityType() == Constants.ENTITY_TYPE_NOTE) {
            Note target = noteDao.queryById(comment.getEntityId());
            commentEvent.setEntityUserId(target.getUserId());
        } else {
            Comment target = commentDao.queryById(comment.getEntityId());
            commentEvent.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(commentEvent);

        return null;
    }

    /**
     * 删除评论[改变评论状态]
     *
     * @param commentId
     * @param userId
     * @return
     */
    @Override
    public String deleteComment(int commentId, int userId) {
        Comment comment = commentDao.queryById(commentId);
        if (comment == null) {
            return "评论不存在!";
        }
        // 防止评论被他人删除
        if (userId != comment.getUserId()) {
            return "无权限!";
        }
        // 使评论失效
        comment.setState(1);
        // 修改数据库
        commentDao.update(comment);
        // 如果是对帖子的评论就要更新帖子的评论数和帖子分数
        if (comment.getEntityType() == Constants.ENTITY_TYPE_NOTE) {
            // 将帖子加入需要更新分数的帖子编号Set中, 等待自动任务更新帖子分数
            String flushScoreKey = RedisKeyUtil.getPostScoreKey();
            redisService.addCacheSet(flushScoreKey, comment.getEntityId());
        }
        return null;
    }

}
