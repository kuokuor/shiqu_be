package com.kuokuor.shiqu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.*;
import com.kuokuor.shiqu.entity.Comment;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.event.Event;
import com.kuokuor.shiqu.event.EventProducer;
import com.kuokuor.shiqu.service.*;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
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
    private CommentDao commentDao;

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private TagDao tagDao;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private FollowService followService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisService redisService;

    /**
     * 新增笔记
     *
     * @param note   笔记对象
     * @param images 图片组
     * @param tags   标签组
     * @return
     */
    @Override
    public String insertNote(Note note, String[] images, int[] tags) {
        if (note == null) {
            return "未获取到帖子信息!";
        }
        // 直接插入数据库
        noteDao.insert(note);

        // 插入img
        imageDao.insertNoteImages(note.getId(), images);

        // 插入tag
        tagDao.insertNoteTags(note.getId(), tags);

        // 同时将数据加入es
        Event event = new Event()
                .setTopic(Constants.TOPIC_PUBLISH)
                .setUserId(note.getUserId())
                .setEntityType(Constants.ENTITY_TYPE_NOTE)
                .setEntityId(note.getId());
        // 发布事件
        eventProducer.fireEvent(event);

        // 将帖子加入需要更新分数的帖子编号Set中, 等待自动任务更新帖子分数
        String flushScoreKey = RedisKeyUtil.getPostScoreKey();
        redisService.addCacheSet(flushScoreKey, note.getId());

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
        // 该请求数量较少, 可以直接把笔记全部查出来
        Note note = noteDao.queryById(noteId);
        if (note == null) {
            return "数据不存在!";
        }
        // 防止删除别人的帖子
        if (note.getUserId() != userId) {
            return "权限不足!";
        }
        // 删除帖子
        noteDao.deleteById(noteId);
        // 触发删帖事件, 从es中删除数据
        Event event = new Event()
                .setTopic(Constants.TOPIC_DELETE)
                .setUserId(userId)
                .setEntityType(Constants.ENTITY_TYPE_NOTE)
                .setEntityId(noteId);
        eventProducer.fireEvent(event);
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
        Map<String, Object> noteDetail = new HashMap<>();
        // 先查询帖子自身的数据
        Note noteInfo = noteDao.queryById(id);
        // 帖子不存在
        if (noteInfo == null) {
            return null;
        }
        // --------------------------------note--------------------------------
        Map<String, Object> note = new HashMap<>();
        note.put("id", noteInfo.getId());
        note.put("title", noteInfo.getTitle());
        note.put("content", noteInfo.getContent());
        note.put("likeCount", likeService.findEntityLikeCount(Constants.ENTITY_TYPE_NOTE, id));
        note.put("editTime", noteInfo.getCreateTime());
        // 当前用户是否对帖子点赞
        boolean hasLike = holderUserId != null
                && likeService.userHasLike(holderUserId, Constants.ENTITY_TYPE_NOTE, id);
        note.put("liked", hasLike);
        note.put("collectCount", collectService.findPostCollectCount(id));
        // 当前用户是否收藏
        boolean hasCollect = holderUserId != null
                && followService.hasFollowed(holderUserId, Constants.ENTITY_TYPE_NOTE, id);
        note.put("collected", hasCollect);
        noteDetail.put("note", note);

        // --------------------------------author--------------------------------
        // 查询帖子作者的数据
        User noteAuthor = userDao.querySimpleUserById(noteInfo.getUserId());
        Map<String, Object> author = new HashMap<>();
        author.put("id", noteAuthor.getId());
        author.put("nickname", noteAuthor.getNickname());
        author.put("avatar", noteAuthor.getAvatar());
        // 当前用户是否关注这一用户
        author.put("followed", followService.hasFollowed(holderUserId, Constants.ENTITY_TYPE_USER, noteAuthor.getId()));
        noteDetail.put("author", author);

        // --------------------------------comments--------------------------------
        // 评论: 对帖子的评论
        // 回复: 对评论的评论

        // 查询帖子的所有评论
        List<Comment> commentList = commentDao.queryCommentsByEntity(Constants.ENTITY_TYPE_NOTE, noteInfo.getId());
        // 对每个评论进行处理[放入用户数据, 评论的回复等数据]
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 一个评论的VO
                Map<String, Object> commentVo = new HashMap<>();
                // 往VO里添加评论
                commentVo.put("id", comment.getId());
                commentVo.put("commentText", comment.getContent());
                commentVo.put("commentTime", comment.getCreateTime());
                // 评论的作者
                commentVo.put("user", userDao.querySimpleUserById(comment.getUserId()));

                // 评论的赞
                long likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 当前用户是否点赞
                hasLike = holderUserId != null
                        && userDao.queryById(holderUserId) != null
                        && likeService.userHasLike(holderUserId, Constants.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("liked", hasLike);

                // 回复列表
                List<Comment> replyList = commentDao.queryCommentsByEntity(Constants.ENTITY_TYPE_COMMENT, comment.getId());
                // 回复的VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        commentVo.put("id", reply.getId());
                        replyVo.put("replyText", reply.getContent());
                        commentVo.put("replyTime", reply.getCreateTime());
                        // 作者
                        replyVo.put("user", userDao.querySimpleUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userDao.querySimpleUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 回复的赞
                        likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // 当前用户是否对回复点赞
                        hasLike = holderUserId != null
                                && userDao.queryById(holderUserId) != null
                                && likeService.userHasLike(holderUserId, Constants.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("liked", hasLike);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("reply", replyVoList);

                commentVoList.add(commentVo);
            }
        }

        noteDetail.put("comments", commentVoList);

        // --------------------------------images--------------------------------
        // Images
        String[] imageList = imageDao.selectNoteImages(noteInfo.getId());
        noteDetail.put("images", imageList);

        return noteDetail;
    }

    /**
     * 得到指定类型的笔记数量
     *
     * @param type -1则为所有
     * @return
     */
    @Override
    public long getNoteCount(int type) {
        Note post = new Note();
        if (type != -1) {
            post.setType(type);
        }
        return noteDao.count(post);
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
            author.put("id", authorInfo.getId());
            author.put("avatar", authorInfo.getAvatar());
            author.put("nickname", authorInfo.getNickname());
            map.put("author", author);

            notes.add(map);
        }
        return notes;
    }

    /**
     * 得到帖子信息
     *
     * @param noteId
     * @return
     */
    public Note getNoteById(int noteId) {
        return noteDao.queryById(noteId);
    }
}
