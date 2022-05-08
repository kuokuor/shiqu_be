package com.kuokuor.shiqu.service;

import com.kuokuor.shiqu.entity.Comment;

/**
 * 评论业务层接口
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 20:32
 */
public interface CommentService {
    /**
     * 新增数据
     *
     * @param comment
     * @return
     */
    String addComment(Comment comment);
}
