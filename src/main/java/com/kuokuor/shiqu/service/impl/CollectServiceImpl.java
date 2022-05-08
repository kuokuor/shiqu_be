package com.kuokuor.shiqu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.NoteDao;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.CollectService;
import com.kuokuor.shiqu.service.LikeService;
import com.kuokuor.shiqu.service.RedisService;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 收藏帖子业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 16:28
 */
@Service
public class CollectServiceImpl implements CollectService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserDao userDao;

    /**
     * 帖子被收藏数量
     *
     * @param postId
     * @return
     */
    @Override
    public long findPostCollectCount(int postId) {
        String redisKey = RedisKeyUtil.getFollowerKey(Constants.ENTITY_TYPE_NOTE, postId);
        return redisService.getZSetSize(redisKey);
    }


    /**
     * 查询用户收藏的帖子列表
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> getCollectedPostList(int userId, int offset, int limit) {
        String redisKey = RedisKeyUtil.getFolloweeKey(userId, Constants.ENTITY_TYPE_NOTE);
        // 按分数从小到大返回set中的值, 也就是按时间从近到远
        Set<Integer> followeeIdSet = redisTemplate.opsForZSet()
                .reverseRange(redisKey, offset, offset + limit - 1);
        // 如果用户没有关注过任何人就会返回空
        if (followeeIdSet == null)
            return null;

        // 用户List对上述结果进行加工
        List<Map<String, Object>> collectedPostList = new ArrayList<>();
        for (Integer noteId : followeeIdSet) {
            Map<String, Object> map = new HashMap<>();
            Note note = noteDao.queryById(noteId);
            // 防止帖子被删除而不存在
            if (note == null) {
                continue;
            }
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
                liked = likeService.userHasLike(StpUtil.getLoginIdAsInt(), Constants.ENTITY_TYPE_NOTE, note.getId());
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

            collectedPostList.add(map);
        }
        return collectedPostList;
    }

}