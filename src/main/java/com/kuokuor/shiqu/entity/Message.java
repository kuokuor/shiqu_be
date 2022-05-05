package com.kuokuor.shiqu.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息表(Message)实体类
 *
 * @author makejava
 * @since 2022-05-05 19:48:47
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 282931276405462134L;
    /**
     * 主键
     */
    private Integer id;
    /**
     * 消息发送者
     */
    private Integer fromId;
    /**
     * 消息接收者
     */
    private Integer toId;
    /**
     * 会话ID[用于将同一会话一次性去除而进行冗余]
     */
    private String conversationId;
    /**
     * 状态[0-未读 1-已读 2-删除]
     */
    private Integer state;
    /**
     * 发送时间
     */
    private Date createTime;
    /**
     * 消息内容
     */
    private String content;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

