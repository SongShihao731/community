package com.songshihao.community.dao;

import com.songshihao.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
// 因为采用redis获取ticket，所以通过传统方式获取ticket的方式不推荐使用了，现在增加注解@Deprecated
@Deprecated
public interface LoginTicketMapper {
    // 插入
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    // 配置主键自增长并重新注入
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    // 查询
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    // 更新状态
    @Update({
            "<script> ",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if> ",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
