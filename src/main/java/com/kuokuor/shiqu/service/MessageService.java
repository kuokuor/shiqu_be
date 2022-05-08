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
}
