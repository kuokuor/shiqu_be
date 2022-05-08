package com.kuokuor.shiqu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.NoteDao;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.LikeService;
import com.kuokuor.shiqu.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 笔记业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 19:57
 */
@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private LikeService likeService;

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
    @Override
    public List<Map<String, Object>> queryAllByLimit(int userId, int offset, int limit, int orderMode) {
        // 并且此处的帖子信息已被优化, 没必要全部传输
        List<Note> noteList = noteDao.queryAllByLimit(userId, offset, limit, orderMode);
        if (noteList == null || noteList.size() == 0)
            return null;

        // 把帖子相关信息封装起来传输
        List<Map<String, Object>> notes = new ArrayList<>();
        for (Note note : noteList) {
            Map<String, Object> map = new HashMap<>();

            // note
            Map<String, Object> noteInfo = new HashMap<>();
            noteInfo.put("id", note.getId());
            noteInfo.put("title", note.getTitle());
            noteInfo.put("editTime", note.getCreateTime());
            noteInfo.put("headerImg", note.getHeadImg());
            // 点赞数据处理
            noteInfo.put("likeCount", likeService.findEntityLikeCount(Constants.ENTITY_TYPE_NOTE, note.getId()));
            boolean liked = false;
            // 如果当前有用户登录且点赞了
            if (StpUtil.isLogin()) {
                likeService.userHasLike(StpUtil.getLoginIdAsInt(), Constants.ENTITY_TYPE_NOTE, note.getId());
            }
            noteInfo.put("liked", liked);
            map.put("note", noteInfo);

            // author
            Map<String, Object> author = new HashMap<>();
            User authorInfo = userDao.querySimpleUserById(note.getUserId());
            author.put("avatar", authorInfo.getAvatar());
            author.put("nickname", authorInfo.getNickname());
            map.put("author", author);

            notes.add(map);
        }
        return notes;
    }
}
