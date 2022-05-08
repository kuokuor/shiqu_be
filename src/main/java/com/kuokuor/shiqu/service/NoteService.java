package com.kuokuor.shiqu.service;

import com.kuokuor.shiqu.entity.Note;

import java.util.List;
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
     * @param tags   标签组
     * @return
     */
    String insertNote(Note note, String[] images, int[] tags);

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

    /**
     * 查询指定行数据[用户id不为0就查询指定用户, 否则查询所有--先按top排序保证顶置在最前]
     * [orderMode为1则按分数再按时间排序 为0则按时间排序 为2则只按时间排序]
     *
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode
     * @return
     */
    List<Map<String, Object>> queryAllByLimit(int userId, int offset, int limit, int orderMode);

    /**
     * 得到帖子信息
     *
     * @param noteId
     * @return
     */
    Note getNoteById(int noteId);

    /**
     * 查询指定tag下的所有笔记
     *
     * @param tag
     * @return
     */
    List<Map<String, Object>> classify(int tag);

    /**
     * 查询用户的关注的笔记
     *
     * @param holderId
     * @param offset
     * @param limit
     * @return
     */
    List<Map<String, Object>> queryFolloweeNotes(int holderId, int offset, int limit);

}
