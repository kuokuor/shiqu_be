package com.kuokuor.shiqu.service;

import java.util.List;
import java.util.Map;

/**
 * 消息业务层接口
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/7 17:15
 */
public interface MessageService {

    /**
     * 发送私信[发送系统通知在消息队列里实现]
     *
     * @param fromId
     * @param toId
     * @param content
     * @return
     */
    String sendLetter(int fromId, int toId, String content);

    /**
     * 得到私信详情
     *
     * @param userId
     * @param conversationId
     * @return
     */
    Map<String, Object> getLetterDetail(int userId, String conversationId);

    /**
     * 得到消息首页的私信列表
     *
     * @param userId
     * @return
     */
    List<Map<String, Object>> getLetterHome(int userId);

    /**
     * 获取系统通知未读数量
     *
     * @param holderId
     * @return
     */
    Map<String, Integer> getNoticeUnreadCount(int holderId);

    /**
     * 查询所有消息的未读数量
     *
     * @param holderId
     * @return
     */
    int getUnreadCount(int holderId);

    /**
     * 获取通知列表[点赞、收藏、评论]
     *
     * @param holderId
     * @param type
     * @return
     */
    List<Map<String, Object>> getNoticeList(int holderId, String type);

    /**
     * 获取关注通知列表
     *
     * @param holderId
     * @return
     */
    List<Map<String, Object>> getFollowNoticeList(int holderId);
}
