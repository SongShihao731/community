package com.songshihao.community.dao;

import com.songshihao.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    // @Param注解用于给参数取别名
    // 如果只有一个参数，并且在动态sql中如<if>中使用，则必须加别名。
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    // 更新评论的数量
    int updateCommentCount(int id, int commentCount);

    // 更改帖子类型
    int updateType(int id, int type);

    // 更改帖子状态
    int updateStatus(int id, int status);

    // 更改帖子分数
    int updateScore(int id, double score);
}
