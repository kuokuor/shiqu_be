package com.kuokuor.shiqu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.UserDao;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.entity.User;
import com.kuokuor.shiqu.service.LikeService;
import com.kuokuor.shiqu.service.SearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索业务层
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 19:55
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private UserDao userDao;

    @Autowired
    private LikeService likeService;


    /**
     * 保存笔记
     *
     * @param note
     */
    @Override
    public void saveNote(Note note) {
        elasticsearchRestTemplate.save(note);
    }

    /**
     * 删除帖子
     *
     * @param noteId
     */
    @Override
    public void deleteNote(int noteId) {
        elasticsearchRestTemplate.delete(String.valueOf(noteId), Note.class);
    }

    /**
     * 查询
     *
     * @param text
     * @param current
     * @param limit
     * @param type    帖子类型[为空时则查询所有帖子]
     * @return
     */
    @Override
    public List<Map<String, Object>> searchPostList(String text, int current, int limit, Integer type) {
        NativeSearchQuery searchQuery;

        // 没有指定类型就全部查询, 指定了就查询固定类型
        if (type == null) {
            searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.multiMatchQuery(text, "title", "content"))
                    .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                    .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                    .withPageable(PageRequest.of(current, limit))
                    .build();
        } else {
            searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.multiMatchQuery(text, "title", "content"))
                            .must(QueryBuilders.matchQuery("type", type)))
                    .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                    .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                    .withPageable(PageRequest.of(current, limit))
                    .build();
        }

        List<SearchHit<Note>> searchHits = elasticsearchRestTemplate.search(searchQuery, Note.class).getSearchHits();

        // 处理查询结果
        if (searchHits.isEmpty()) {
            return new ArrayList<>();
        }

        // 把帖子相关信息封装起来传输
        List<Map<String, Object>> notes = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            Note note = (Note) hit.getContent();
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

            notes.add(map);
        }

        return notes;
    }
}
