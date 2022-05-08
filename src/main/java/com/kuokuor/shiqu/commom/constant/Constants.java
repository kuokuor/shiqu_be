package com.kuokuor.shiqu.commom.constant;

/**
 * 通用常量类
 *
 * @Author: GreatBiscuit
 * @Date: 2022/4/9 16:06
 */
public class Constants {

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;

    /**
     * 找回密码验证码有效期(5分钟)
     */
    public static final long FIND_PASSWORD_CODE_EXPIRATION = 5;

    /**
     * 登录时采取记住我模式
     */
    public static final Integer REMEMBER_ME = 60 * 60 * 24 * 7;

    /**
     * 实体类型 帖子
     */
    public static final Integer ENTITY_TYPE_NOTE = 1;

    /**
     * 实体类型 评论
     */
    public static final Integer ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型 用户
     */
    public static final Integer ENTITY_TYPE_USER = 3;

    /**
     * 主题 评论
     */
    public static final String TOPIC_COMMENT = "shiqu_comment";

    /**
     * 主题 点赞
     */
    public static final String TOPIC_LIKE = "shiqu_like";

    /**
     * 主题 关注
     */
    public static final String TOPIC_FOLLOW = "shiqu_follow";

    /**
     * 主题 收藏
     */
    public static final String TOPIC_COLLECT = "shiqu_collect";

    /**
     * 主题 发帖
     */
    public static final String TOPIC_PUBLISH = "shiqu_publish";

    /**
     * 主题 删帖
     */
    public static final String TOPIC_DELETE = "shiqu_delete";

    /**
     * 主题 发送邮件
     */
    public static final String TOPIC_SEND_MAIL = "shiqu_sendMail";

    /**
     * 系统用户ID
     */
    public static final Integer SYSTEM_USER_ID = 0;

    /**
     * 用户注销状态
     */
    public static final Integer USER_TYPE_DESTROY = 886;

    /**
     * Quart自动任务时间
     */
    public static final Integer QUARTZ_JOB_TIME = 1000 * 60 * 5; // TODO: 正式上线需要修改时间
}
