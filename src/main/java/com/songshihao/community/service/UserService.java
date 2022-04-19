package com.songshihao.community.service;

import com.songshihao.community.dao.UserMapper;
import com.songshihao.community.entity.User;
import com.songshihao.community.util.CommunityConstant;
import com.songshihao.community.util.CommunityUtil;
import com.songshihao.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 用户注册逻辑
     * @param user 封装好的用户输入的信息
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 1.判断空值的情况
        // 1.1 判断user为null的情况
        if (user == null) {
            // 抛出异常
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 1.2 判断username为空的情况
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        // 1.3 判断password为空的情况
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        // 1.4 判断email为空的情况
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        // 2.验证账号
        // 2.1 验证账号名是否已经存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已经存在");
            return map;
        }
        // 2.2 验证邮箱是否已经存在
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已经被注册");
            return map;
        }

        // 3.注册用户
        // 3.1 加密密码
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 3.2 设置用户类型为普通用户
        user.setType(0);
        // 3.3 设置用户的状态为未激活
        user.setStatus(0);
        // 3.4 设置激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 3.5 设置随机头像
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",
                new Random().nextInt(1000)));
        // 3.6 设置生成日期
        user.setCreateTime(new Date());

        // 4.添加到数据库中
        userMapper.insertUser(user);

        // 5. 发送激活邮件
        // 5.1 传入参数
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 要求激活的路径如下: http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" +
                user.getActivationCode();
        context.setVariable("url", url);
        //5.2 生成邮件内容
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮箱", content);

        return map;
    }

    /**
     * 邮箱激活逻辑
     * @param userId 传入的用户id
     * @param code 传入的激活码
     * @return 自己定义激活状态码
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUEECEE;
        }else {
            return ACTIVATION_FAILURE;
        }
    }
}
