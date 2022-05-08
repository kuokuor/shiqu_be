package com.kuokuor.shiqu.service.impl;

import com.kuokuor.shiqu.dao.MessageDao;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.Message;
import com.kuokuor.shiqu.entity.User;
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
            conversationVo.put("from", from);

            conversationListVo.add(conversationVo);
        }

        return conversationListVo;
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
