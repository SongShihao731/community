package com.songshihao.community.dao;

import com.songshihao.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 根据实体查询评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询评论总数
    int selectCountByEntity(int entityType, int entityId);
}
