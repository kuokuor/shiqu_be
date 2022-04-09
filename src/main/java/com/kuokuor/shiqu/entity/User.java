package com.kuokuor.shiqu.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户(User)实体类
 *
 * @author makejava
 * @since 2022-04-09 16:09:33
 */
public class User implements Serializable {
    private static final long serialVersionUID = 353328394670629407L;
    /**
     * 用户编号
     */
    private Integer id;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码
     */
    private String password;
    /**
     * 加密盐值
     */
    private String salt;
    /**
     * 用户头像地址
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 性别[0-未知 1-男 2-女]
     */
    private Integer sex;
    /**
     * 简介
     */
    private String description;
    /**
     * 用户类型[0-普通用户 886-注销]
     */
    private Integer type;
    /**
     * 创建时间
     */
    private Date createTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}

