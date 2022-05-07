package com.songshihao.community.service;

import com.songshihao.community.dao.CommentMapper;
import com.songshihao.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    // 根据实体查询评论
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
       return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    // 根据实体查询评论总数
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }


}
