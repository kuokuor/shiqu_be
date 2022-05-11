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

}
