package com.kuokuor.shiqu.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (Tag)表数据库访问层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 23:01
 */
@Mapper
public interface TagDao {

    /**
     * 插入编号为noteId的笔记的tag
     *
     * @param noteId
     * @param tags
     * @return
     */
    int insertNoteTags(int noteId, @Param("tags") int[] tags);

    /**
     * 删除编号为noteId的笔记的tag
     *
     * @param noteId
     * @return
     */
    int deleteNoteTags(int noteId);

    /**
     * 查询编号为noteId的笔记的tag列表
     *
     * @param noteId
     * @return
     */
    int[] selectNoteTags(int noteId);

    /**
     * 查询指定tag的所有帖子
     *
     * @param tagId
     * @return
     */
    int[] selectNoteByTag(int tagId);

}
