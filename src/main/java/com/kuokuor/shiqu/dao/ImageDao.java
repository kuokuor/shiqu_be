package com.kuokuor.shiqu.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (Image)表数据库访问层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 22:36
 */
@Mapper
public interface ImageDao {

    /**
     * 插入编号为noteId的笔记的图片
     *
     * @param noteId
     * @param images
     * @return
     */
    int insertNoteImages(int noteId, @Param("images") String[] images);

    /**
     * 删除编号为noteId的笔记的图片
     *
     * @param noteId
     * @return
     */
    int deleteNoteImages(int noteId);

    /**
     * 查询编号为noteId的笔记的图片
     *
     * @param noteId
     * @return
     */
    String[] selectNoteImages(int noteId);

}
