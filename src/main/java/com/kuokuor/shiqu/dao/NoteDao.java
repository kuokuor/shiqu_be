package com.kuokuor.shiqu.dao;

import com.kuokuor.shiqu.entity.Note;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * (Note)表数据库访问层
 *
 * @author makejava
 * @since 2022-05-05 19:37:37
 */
@Mapper
public interface NoteDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Note queryById(Integer id);

    /**
     * 统计总行数
     *
     * @param note 查询条件
     * @return 总行数
     */
    long count(Note note);

    /**
     * 新增数据
     *
     * @param note 实例对象
     * @return 影响行数
     */
    int insert(Note note);

    /**
     * 修改数据
     *
     * @param note 实例对象
     * @return 影响行数
     */
    int update(Note note);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 查询指定行数据[用户id不为0就查询指定用户, 否则查询所有--先按top排序保证顶置在最前]
     * [orderMode为1则按分数再按时间排序 为0则按时间排序]
     * [只针对首页帖子列表查询出了必要的数据]
     *
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode
     * @return
     */
    List<Note> queryAllByLimit(int userId, int offset, int limit, int orderMode);
}

