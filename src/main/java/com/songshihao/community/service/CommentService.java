package com.songshihao.community.service;

import com.songshihao.community.dao.CommentMapper;
import com.songshihao.community.entity.Comment;
import com.songshihao.community.util.CommunityConstant;
import com.songshihao.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    // 根据实体查询评论
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
       return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    // 根据实体查询评论总数
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 增加帖子评论（进行事务控制）
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        // 空值报错
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 对帖子内容进行过滤
        // 1. 过滤评论的标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 2. 过滤评论的内容
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 3. 添加帖子
        int rows = commentMapper.insertComment(comment);

        // 增加帖子数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 1.查询帖子评论数量
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            // 2.更新帖子评论的数量
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }


}
