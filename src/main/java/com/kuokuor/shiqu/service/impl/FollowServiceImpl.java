package com.kuokuor.shiqu.service.impl;

import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.event.Event;
import com.kuokuor.shiqu.event.EventProducer;
import com.kuokuor.shiqu.service.FollowService;
import com.kuokuor.shiqu.service.RedisService;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 关注业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 15:00
 */
@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注/收藏
     *
     * @param entityType   实体类型
     * @param entityId     实体Id
     * @param entityUserId 实体所属用户
     * @return
     */
    @Override
    public void follow(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                redisOperations.multi();
                long time = System.currentTimeMillis();
                redisTemplate.opsForZSet().add(followeeKey, entityId, time);
                redisTemplate.opsForZSet().add(followerKey, userId, time);
                return redisOperations.exec();
            }
        });
        // 发送系统通知
        Event event = new Event()
                .setTopic(entityType == Constants.ENTITY_TYPE_NOTE ?
                        Constants.TOPIC_COLLECT : Constants.TOPIC_FOLLOW) // 判断是收藏帖子还是关注用户
                .setUserId(userId)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityUserId);
        // 发布事件
        eventProducer.fireEvent(event);

        // 如果是收藏帖子就需要刷新帖子分数
        if (entityType == Constants.ENTITY_TYPE_NOTE) {
            // 将帖子加入需要更新分数的帖子编号Set中, 等待自动任务更新帖子分数
            String flushScoreKey = RedisKeyUtil.getPostScoreKey();
            redisService.addCacheSet(flushScoreKey, entityId);
        }
    }

    /**
     * 取消关注
     *
     * @param userId     关注者
     * @param entityType 被关注实体类型
     * @param entityId   被关注实体ID
     */
    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                redisOperations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
        // 取消关注就不发送系统通知了
    }

    /**
     * 本用户关注了多少人
     *
     * @param userId
     * @return
     */
    @Override
    public long queryFollowCount(int userId) {
        String redisKey = RedisKeyUtil.getFolloweeKey(userId, Constants.ENTITY_TYPE_USER);
        Long count = redisService.getZSetSize(redisKey);
        // 防止没有数据而导致空指针
        return count == null ? 0L : count;
    }

    /**
     * 该用户的粉丝数量
     *
     * @param userId
     * @return
     */
    @Override
    public long queryFansCount(int userId) {
        String redisKey = RedisKeyUtil.getFollowerKey(Constants.ENTITY_TYPE_USER, userId);
        Long count = redisService.getZSetSize(redisKey);
        // 防止没有数据而导致空指针
        return count == null ? 0L : count;
    }

    /**
     * 该用户是否关注了该实体
     *
     * @param userId     关注者
     * @param entityType 被关注实体类型
     * @param entityId   被关注实体ID
     * @return
     */
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String redisKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisService.isMemberOfZSet(redisKey, entityId);
    }

    /**
     * 查询该用户关注的人的列表[分页]
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> queryFolloweeList(Integer holderId, int userId, int offset, int limit) {
        String redisKey = RedisKeyUtil.getFolloweeKey(userId, Constants.ENTITY_TYPE_USER);
        // 按分数从小到大返回set中的值, 也就是按时间从近到远
        Set<Integer> followeeIdSet = redisTemplate.opsForZSet()
                .reverseRange(redisKey, offset, offset + limit - 1);
        // 如果用户没有关注过任何人就会返回空
        if (followeeIdSet == null)
            return null;

        // 用户List对上述结果进行加工
        List<Map<String, Object>> followeeInfo = new ArrayList<>();
        for (Integer followeeId : followeeIdSet) {
            Map<String, Object> map = new HashMap<>();
            // 用简单信息就够了
            User user = userDao.querySimpleUserById(followeeId);
            map.put("user", user);
            // 把关注的时间转换为时间格式返回去
            Double score = redisService.getZSetScore(redisKey, followeeId);
            map.put("followTime", new Date(score.longValue()));
            // 看当前系统用户是否关注了[当前系统用户没登录则判断为没登录]
            map.put("hasFollowed",
                    holderId != null && hasFollowed(holderId, Constants.ENTITY_TYPE_USER, followeeId));
            // 把加工好的信息放入list
            followeeInfo.add(map);
        }
        return followeeInfo;
    }

    /**
     * 查询该用户的粉丝的列表[分页]
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> queryFansList(Integer holderId, int userId, int offset, int limit) {
        String redisKey = RedisKeyUtil.getFollowerKey(Constants.ENTITY_TYPE_USER, userId);
        // 按分数从小到大返回set中的值, 也就是按时间从近到远
        Set<Integer> fansIdSet = redisTemplate.opsForZSet()
                .reverseRange(redisKey, offset, offset + limit - 1);
        // 如果用户没有粉丝就会返回空
        if (fansIdSet == null)
            return null;

        // 用户List对上述结果进行加工
        List<Map<String, Object>> fansInfo = new ArrayList<>();
        for (Integer fansId : fansIdSet) {
            Map<String, Object> map = new HashMap<>();
            // 用简单信息就够了
            User user = userDao.querySimpleUserById(fansId);
            map.put("user", user);
            // 把关注的时间转换为时间格式返回去
            Double score = redisService.getZSetScore(redisKey, fansId);
            map.put("followTime", new Date(score.longValue()));
            // 看当前系统用户是否关注了[当前系统用户没登录则判断为没登录]
            map.put("hasFollowed",
                    holderId != null && hasFollowed(holderId, Constants.ENTITY_TYPE_USER, fansId));
            // 把加工好的信息放入list
            fansInfo.add(map);
        }
        return fansInfo;
    }

}
