package com.kuokuor.shiqu.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * (Note)实体类
 *
 * @author makejava
 * @since 2022-05-05 19:37:37
 */
public class Note implements Serializable {
    private static final long serialVersionUID = -23681811701132043L;
    /**
     * 笔记编号[主键]
     */
    private Integer id;
    /**
     * 作者编号
     */
    private Integer userId;
    /**
     * 笔记标题
     */
    private String title;
    /**
     * 笔记类型
     */
    private Integer type;
    /**
     * 笔记正文内容
     */
    private String content;
    /**
     * 笔记创建时间
     */
    private Date createTime;
    /**
     * 笔记分数
     */
    private Double score;
    /**
     * 头图
     */
    private String headImg;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

}

