package com.kuokuor.shiqu.service.impl;

import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.service.NoteService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 笔记业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 19:57
 */
@Service
public class NoteServiceImpl implements NoteService {

    /**
     * 新增笔记
     *
     * @param note   笔记对象
     * @param images 图片组
     * @return
     */
    @Override
    public String insertNote(Note note, String[] images) {
        return null;
    }

    /**
     * 删除笔记
     *
     * @param userId
     * @param noteId
     * @return
     */
    @Override
    public String deleteNote(int userId, int noteId) {
        return null;
    }

    /**
     * 通过ID查询单条数据
     *
     * @param id
     * @param holderUserId
     * @return
     */
    @Override
    public Map<String, Object> queryNoteDetailById(Integer id, Integer holderUserId) {
        return null;
    }

    /**
     * 得到指定类型的笔记数量
     *
     * @param type -1则为所有
     * @return
     */
    @Override
    public long getNoteCount(int type) {
        return 0;
    }
}
