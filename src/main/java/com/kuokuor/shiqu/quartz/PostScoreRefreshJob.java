package com.kuokuor.shiqu.quartz;

import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.CommentDao;
import com.kuokuor.shiqu.dao.NoteDao;
import com.kuokuor.shiqu.entity.Comment;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.service.RedisService;
import com.kuokuor.shiqu.utils.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.BoundSetOperations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 帖子分数更新任务
 *
 * @Author: GreatBiscuit
 * @Date: 2022/3/31 20:32
 */
public class PostScoreRefreshJob implements Job {

    // 系统起始时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化系统起始时间失败!", e);
        }
    }

    @Autowired
    private RedisService redisService;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 执行任务 刷新笔记分数[分数以Set存在Redis里]
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations boundSetOperations = redisService.getBoundSetOperations(redisKey);

        // 若其中为空则不进行刷新, 任务到此结束
        if (boundSetOperations.size() == 0) {
            return;
        }

        // 如果不为空, 则开始刷新帖子分数任务
        while (boundSetOperations.size() > 0) {
            this.refresh((Integer) boundSetOperations.pop());
        }
    }

    /**
     * 刷新帖子分数
     *
     * @param noteId
     */
    private void refresh(int noteId) {
        Note note = noteDao.queryById(noteId);
        //防止笔记被删
        if (note == null) {
            return;
        }
        // 评论数量
        Comment comment = new Comment();
        comment.setEntityType(Constants.ENTITY_TYPE_NOTE);
        comment.setEntityId(noteId);
        long commentCount = commentDao.count(comment);
        // 点赞数量
        String likeKey = RedisKeyUtil.getEntityLikeKey(Constants.ENTITY_TYPE_NOTE, noteId);
        long likeCount = redisService.getSetSize(likeKey);
        // 收藏加分
        String collectKey = RedisKeyUtil.getFollowerKey(Constants.ENTITY_TYPE_NOTE, noteId);
        long collectCount = redisService.getZSetSize(collectKey);

        // 计算权重
        double w = commentCount * 10 + likeCount * 2 + collectCount * 20;
        // 分数 = 权重 + 天数
        double score = Math.log10(Math.max(w, 1)) + (note.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新帖子分数
        note.setScore(score);
        noteDao.update(note);

        // 同步到ES
        elasticsearchRestTemplate.save(note);
    }
}
