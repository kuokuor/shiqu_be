package com.kuokuor.shiqu.service;

import com.kuokuor.shiqu.entity.Note;

import java.util.Map;

/**
 * 笔记业务层接口
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 19:52
 */
public interface NoteService {
    /**
     * 新增笔记
     *
     * @param note   笔记对象
     * @param images 图片组
     * @return
     */
    String insertNote(Note note, String[] images);

    /**
     * 删除笔记
     *
     * @param userId
     * @param noteId
     * @return
     */
    String deleteNote(int userId, int noteId);

    /**
     * 通过ID查询单条数据
     *
     * @param id
     * @param holderUserId
     * @return
     */
    Map<String, Object> queryNoteDetailById(Integer id, Integer holderUserId);

    /**
     * 得到指定类型的笔记数量
     *
     * @param type -1则为所有
     * @return
     */
    long getNoteCount(int type);
}
