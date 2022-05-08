package com.kuokuor.shiqu.service;

import com.kuokuor.shiqu.entity.Note;

import java.util.List;
import java.util.Map;

/**
 * 搜索业务层接口
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 19:54
 */
public interface SearchService {

    /**
     * 保存笔记
     *
     * @param note
     */
    void saveNote(Note note);

    /**
     * 删除笔记
     *
     * @param noteId
     */
    void deleteNote(int noteId);

    /**
     * 查询
     *
     * @param text
     * @param current
     * @param limit
     * @param type    笔记类型[为空时则查询所有帖子]
     * @return
     */
    List<Map<String, Object>> searchPostList(String text, int current, int limit, Integer type);

}