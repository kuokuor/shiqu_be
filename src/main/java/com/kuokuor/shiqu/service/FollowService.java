package com.kuokuor.shiqu.service;

import java.util.List;
import java.util.Map;

/**
 * 关注业务层接口
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 14:59
 */
public interface FollowService {

    /**
     * 关注/收藏
     *
     * @param entityType   实体类型
     * @param entityId     实体Id
     * @param entityUserId 实体所属用户
     * @return
     */
    void follow(int userId, int entityType, int entityId, int entityUserId);

    /**
     * 取消关注
     *
     * @param userId     关注者
     * @param entityType 被关注实体类型
     * @param entityId   被关注实体ID
     */
    void unfollow(int userId, int entityType, int entityId);

    /**
     * 本用户关注了多少人
     *
     * @param userId
     * @return
     */
    long queryFollowCount(int userId);

    /**
     * 该用户的粉丝数量
     *
     * @param userId
     * @return
     */
    long queryFansCount(int userId);

    /**
     * 该用户是否关注了该实体
     *
     * @param userId     关注者
     * @param entityType 被关注实体类型
     * @param entityId   被关注实体ID
     * @return
     */
    boolean hasFollowed(int userId, int entityType, int entityId);

    /**
     * 查询该用户关注的人的列表[分页]
     *
     * @param holderId 当前系统用户[未登录则为空]
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Map<String, Object>> queryFolloweeList(Integer holderId, int userId, int offset, int limit);

    /**
     * 查询该用户的粉丝的列表[分页]
     *
     * @param holderId 当前系统用户[未登录则为空]
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Map<String, Object>> queryFansList(Integer holderId, int userId, int offset, int limit);

}
