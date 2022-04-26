package com.kuokuor.shiqu.utils;

/**
 * 生成RedisKey的工具类
 *
 * @Author: GreatBiscuit
 * @Date: 2022/3/22 18:12
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_POST = "note";
    private static final String FIND_PASSWORD_CODE = "find_password_code";
    private static final String REGISTER_CODE = "register_code";

    /**
     * 生成注册时的验证码Key
     *
     * @param email
     * @return
     */
    public static String getRegisterCodeKey(String email) {
        return REGISTER_CODE + SPLIT + email;
    }

    /**
     * 生成找回密码时的验证码Key
     *
     * @param email
     * @return
     */
    public static String getFindPasswordCodeKey(String email) {
        return FIND_PASSWORD_CODE + SPLIT + email;
    }

    /**
     * 生成某个实体的赞
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        //like:entity:entityType:entityId -> set(userId)
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户的赞
     *
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId) {
        //like:user:userId -> int
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注了谁
     *
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        // followee:userId:entityType -> zset(entityId,now)
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体的粉丝 帖子也可以被关注[收藏]
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        // follower:entityType:entityId -> zset(userId,now)
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 用户
     *
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 得到帖子分数
     *
     * @return
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

}
