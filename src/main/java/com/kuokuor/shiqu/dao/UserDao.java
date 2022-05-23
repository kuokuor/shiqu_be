package com.kuokuor.shiqu.dao;

import com.kuokuor.shiqu.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户表(User)表数据库访问层
 *
 * @author makejava
 * @since 2022-04-09 16:16:13
 */
@Mapper
public interface UserDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    User queryById(Integer id);

    /**
     * 通过Email查询单条数据
     *
     * @param email 邮箱
     * @return 实例对象
     */
    User queryByEmail(String email);

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 修改数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    int update(User user);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 查询出简略的用户信息
     *
     * @param id
     * @return
     */
    User querySimpleUserById(Integer id);

    /**
     * 搜索用户
     *
     * @param key
     * @return
     */
    List<User> searchByNickname(String key);

    /**
     * 查询用户类型
     *
     * @param userId
     * @return
     */
    int getUserType(int userId);

    /**
     * 统计用户
     *
     * @param user
     * @return
     */
    long count(User user);

    /**
     * 获取所有用户数据
     *
     * @return
     */
    List<User> getAllUser();
}

