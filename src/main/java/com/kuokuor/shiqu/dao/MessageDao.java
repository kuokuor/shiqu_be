package com.kuokuor.shiqu.dao;

import com.kuokuor.shiqu.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消息表(Message)表数据库访问层
 *
 * @author makejava
 * @since 2022-05-05 19:48:47
 */
@Mapper
public interface MessageDao {

    /**
     * 查询私信列表[返回每个私信最新的一条消息]
     *
     * @param userId
     * @return
     */
    List<Message> queryConversations(int userId);

    /**
     * 查询私信详情[返回私信的所有数据]
     *
     * @param conversationId
     * @return
     */
    List<Message> queryLetters(String conversationId);

    /**
     * 查询未读私信数量[conversationId为null时查询未读私信总数]
     *
     * @param userId
     * @param conversationId
     * @return
     */
    int queryLetterUnreadCount(int userId, String conversationId);

    /**
     * 新增消息[私信和系统通知都是消息]
     *
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 查询未读通知数
     *
     * @param userId
     * @param topic
     * @return
     */
    int queryNoticeUnreadCount(int userId, String topic);

    /**
     * 查询某一类型的通知详情
     *
     * @param userId
     * @param topic
     * @return
     */
    List<Message> queryNotices(int userId, String topic);

    /**
     * 修改状态
     *
     * @param ids
     * @param state
     * @return
     */
    int updateState(List<Integer> ids, int state);
}

