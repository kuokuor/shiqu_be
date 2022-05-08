package com.kuokuor.shiqu.service.impl;

import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.CommentDao;
import com.kuokuor.shiqu.event.Event;
import com.kuokuor.shiqu.event.EventProducer;
import com.kuokuor.shiqu.service.LikeService;
import com.kuokuor.shiqu.service.RedisService;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 点赞业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 15:24
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 点赞[取消点赞也是调用该方法, 在此处进行判断]
     *
     * @param userId       当前用户
     * @param entityType   被点赞的实体类型
     * @param entityId     被点赞的实体ID
     * @param entityUserId 被点赞的实体的发布者
     * @param postId       让前端传入, 否则数据库交互过多
     * @return
     */
    @Override
    public String like(int userId, int entityType, int entityId, int entityUserId, int postId) {
        // 此处需要使用Redis的事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                // 开启事务
                operations.multi();

                if (isMember) {
                    // 已点赞，所以现在是取消
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    // 点赞
                    // 实体获赞则放在set里
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    // 统计用户获赞数量
                    redisTemplate.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
        // 查看刚刚是进行点赞还是取消点赞[是点赞就发送系统通知 取消点赞不发]
        if (redisTemplate.opsForSet().isMember(RedisKeyUtil.getEntityLikeKey(entityType, entityId), userId)) {
            Event event = new Event()
                    .setTopic(Constants.TOPIC_LIKE)
                    .setUserId(userId)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            // 发布事件
            eventProducer.fireEvent(event);
        }

        // 如果是对帖子点赞/取消点赞, 就需要更新帖子分数
        if (entityType == Constants.ENTITY_TYPE_NOTE) {
            // 将帖子加入需要更新分数的帖子编号Set中, 等待自动任务更新帖子分数
            String flushScoreKey = RedisKeyUtil.getPostScoreKey();
            redisService.addCacheSet(flushScoreKey, entityId);
        }

        return null;
    }

    /**
     * 查询某个实体获赞的总数
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisService.getSetSize(redisKey);
    }

    /**
     * 用户是否对这一实体点赞
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public boolean userHasLike(int userId, int entityType, int entityId) {
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisService.isMemberOfSet(redisKey, userId);
    }

}