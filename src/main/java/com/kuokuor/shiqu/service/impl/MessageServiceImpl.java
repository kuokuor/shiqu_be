package com.kuokuor.shiqu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.CommentDao;
import com.kuokuor.shiqu.dao.MessageDao;
import com.kuokuor.shiqu.dao.NoteDao;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.Comment;
import com.kuokuor.shiqu.entity.Message;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.FollowService;
import com.kuokuor.shiqu.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 消息业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/7 17:17
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private FollowService followService;

    /**
     * 发送私信[发送系统通知在消息队列里实现]
     *
     * @param fromId
     * @param toId
     * @param content
     * @return
     */
    @Override
    public String sendLetter(int fromId, int toId, String content) {
        // 防止目标用户不存在
        if (userDao.queryById(toId) == null) {
            return "目标用户不存在!";
        }
        Message letter = new Message();
        letter.setFromId(fromId);
        letter.setToId(toId);
        letter.setContent(content);
        // 未读
        letter.setState(0);
        letter.setCreateTime(new Date());
        // 处理会话ID
        if (fromId < toId) {
            letter.setConversationId(fromId + "_" + toId);
        } else {
            letter.setConversationId(toId + "_" + fromId);
        }
        // 添加进数据库
        messageDao.insertMessage(letter);
        return null;
    }

    /**
     * 得到私信详情
     *
     * @param conversationId
     * @return
     */
    @Override
    public Map<String, Object> getLetterDetail(int userId, String conversationId) {
        // 可以得到两个Id
        String[] idPair = conversationId.split("_");
        // 得到对方的Id
        int targetId = userId == Integer.parseInt(idPair[0]) ?
                Integer.parseInt(idPair[1]) : Integer.parseInt(idPair[0]);
        User target = userDao.querySimpleUserById(targetId);
        // 防止对方用户不存在[当前用户已登录, 肯定存在]
        if (target == null) {
            return null;
        }

        // 构造返回数据
        Map<String, Object> map = new HashMap<>();
        // 得到两个用户的基本信息
        map.put("target", target);
        map.put("holder", userDao.querySimpleUserById(userId));
        List<Message> letterList = messageDao.queryLetters(conversationId);
        // 翻转List
        Collections.reverse(letterList);
        map.put("letterList", letterList);
        // 将私信设为已读
        readMessage(letterList, userId);
        return map;
    }

    /**
     * 得到消息首页的私信列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<Map<String, Object>> getLetterHome(int userId) {
        List<Message> conversationList = messageDao.queryConversations(userId);
        // 如果没有就返回null
        if (conversationList == null) {
            return null;
        }
        // 包装私信列表
        List<Map<String, Object>> conversationListVo = new ArrayList<>();
        for (Message conversation : conversationList) {
            Map<String, Object> conversationVo = new HashMap<>();

            // id
            conversationVo.put("id", conversation.getId());

            // letter
            Map<String, Object> letter = new HashMap<>();
            letter.put("unreadCount", messageDao.queryLetterUnreadCount(userId, conversation.getConversationId()));
            letter.put("lastTime", conversation.getCreateTime());
            letter.put("lastLetter", conversation.getContent());
            conversationVo.put("letter", letter);

            // from
            // 对方的Id
            int targetId = userId == conversation.getFromId() ?
                    conversation.getToId() : conversation.getFromId();
            Map<String, Object> from = new HashMap<>();
            User tUser = userDao.querySimpleUserById(targetId);
            from.put("id", tUser.getId());
            from.put("nickname", tUser.getNickname());
            from.put("avatar", tUser.getAvatar());
            from.put("type", tUser.getType());
            conversationVo.put("from", from);

            conversationListVo.add(conversationVo);
        }

        return conversationListVo;
    }

    /**
     * 获取系统通知未读数量
     *
     * @param holderId
     * @return
     */
    @Override
    public Map<String, Integer> getNoticeUnreadCount(int holderId) {
        Map<String, Integer> noticeUnreadCount = new HashMap<>();
        // 点赞通知
        noticeUnreadCount.put("like", messageDao.queryNoticeUnreadCount(holderId, Constants.TOPIC_LIKE));
        // 关注通知
        noticeUnreadCount.put("follow", messageDao.queryNoticeUnreadCount(holderId, Constants.TOPIC_FOLLOW));
        // 评论通知
        noticeUnreadCount.put("comment", messageDao.queryNoticeUnreadCount(holderId, Constants.TOPIC_COMMENT));
        // 收藏通知
        noticeUnreadCount.put("collect", messageDao.queryNoticeUnreadCount(holderId, Constants.TOPIC_COLLECT));

        return noticeUnreadCount;
    }

    /**
     * 查询所有消息的未读数量
     *
     * @param holderId
     * @return
     */
    @Override
    public int getUnreadCount(int holderId) {
        int sum = 0;
        // 未读系统通知数量
        Map<String, Integer> noticeUnreadCount = getNoticeUnreadCount(holderId);
        for (Integer value : noticeUnreadCount.values()) {
            sum += value;
        }
        // 未读私信数量
        sum += messageDao.queryLetterUnreadCount(holderId, null);
        return sum;
    }

    /**
     * 获取通知列表[点赞、收藏、评论]
     *
     * @param holderId
     * @param type
     * @return
     */
    @Override
    public List<Map<String, Object>> getNoticeList(int holderId, String type) {
        // 需要处理type
        String systemType = "shiqu_" + type;
        List<Message> noticeList = messageDao.queryNotices(holderId, systemType);
        if (noticeList == null) {
            return new ArrayList<>();
        }
        // 将私信设为已读
        readMessage(noticeList, holderId);
        List<Map<String, Object>> noticeListVo = new ArrayList<>();
        for (Message notice : noticeList) {
            Map<String, Object> map = new HashMap<>();
            // 得到其他数据
            Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);

            map.put("id", notice.getId());
            map.put("type", type);
            map.put("isUnread", notice.getState() == 0);

            Map<String, Object> targetEntity = new HashMap<>();
            // 先全部置空
            targetEntity.put("headerImg", null);
            targetEntity.put("comment", null);
            if (systemType.equals(Constants.TOPIC_COLLECT)) {
                // 收藏
                map.put("noteId", data.get("entityId"));
                Note note = noteDao.queryById((Integer) data.get("entityId"));
                if (note == null)
                    continue;
                targetEntity.put("headerImg", note.getHeadImg());
            } else if (systemType.equals(Constants.TOPIC_LIKE)) {
                // 点赞
                map.put("noteId", data.get("noteId"));
                Integer likeType = (Integer) data.get("entityType");
                if (Objects.equals(likeType, Constants.ENTITY_TYPE_NOTE)) {
                    Note note = noteDao.queryById((Integer) data.get("entityId"));
                    if (note == null)
                        continue;
                    targetEntity.put("headerImg", note.getHeadImg());
                } else {
                    // 如果是对评论进行点赞就需要记录评论的内容
                    Comment likeComment = commentDao.queryById((Integer) data.get("entityId"));
                    targetEntity.put("comment", likeComment.getContent());
                }
            } else {
                // 评论或回复
                map.put("noteId", data.get("noteId"));

                Integer commentId = (Integer) data.get("commentId");
                // 查出Comment
                Comment comment = commentDao.queryById(commentId);
                if (comment.getEntityType() == Constants.ENTITY_TYPE_COMMENT) {
                    // 如果是对评论进行回复, 通知的类型就需要变为reply
                    map.put("type", "reply");
                    // 如果是回复评论
                    Comment byComment = commentDao.queryById(comment.getEntityId());
                    targetEntity.put("comment", byComment.getContent());
                } else {
                    // 如果是评论帖子
                    Note note = noteDao.queryById(comment.getEntityId());
                    if (note == null)
                        continue;
                    targetEntity.put("headerImg", note.getHeadImg());
                }
                map.put("content", comment.getContent());
            }
            map.put("targetEntity", targetEntity);
            map.put("time", notice.getCreateTime());

            // 处理from
            User user = userDao.querySimpleUserById((Integer) data.get("userId"));
            map.put("from", user);

            noticeListVo.add(map);
        }
        return noticeListVo;
    }

    /**
     * 获取关注通知列表
     *
     * @param holderId
     * @return
     */
    @Override
    public List<Map<String, Object>> getFollowNoticeList(int holderId) {
        List<Message> noticeList = messageDao.queryNotices(holderId, Constants.TOPIC_FOLLOW);
        if (noticeList == null) {
            return new ArrayList<>();
        }
        // 将私信设为已读
        readMessage(noticeList, holderId);
        List<Map<String, Object>> noticeListVo = new ArrayList<>();
        for (Message notice : noticeList) {
            Map<String, Object> map = new HashMap<>();
            // 得到其他数据
            Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);
            int fromId = (Integer) data.get("userId");
            map.put("from", userDao.querySimpleUserById(fromId));
            map.put("id", notice.getId());
            map.put("isUnread", notice.getState() == 0);
            map.put("followed", followService.hasFollowed(holderId, Constants.ENTITY_TYPE_USER, fromId));
            map.put("time", notice.getCreateTime());
            noticeListVo.add(map);
        }
        return noticeListVo;
    }

    /**
     * 将消息设为已读[在用户查看通知或私信详情时被调用]
     *
     * @param messageList
     */
    private void readMessage(List<Message> messageList, int holderUserId) {
        // 防止其为空
        if (messageList == null) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        for (Message message : messageList) {
            // 只改变toId为当前用户的消息的状态
            if (holderUserId == message.getToId() && message.getState() == 0) {
                ids.add(message.getId());
            }
        }
        // 为空则无需处理
        if (ids.isEmpty()) {
            return;
        }
        // 修改状态[1-已读]
        messageDao.updateState(ids, 1);
    }

}
