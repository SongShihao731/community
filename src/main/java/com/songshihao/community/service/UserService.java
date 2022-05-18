package com.songshihao.community.service;

import com.songshihao.community.dao.LoginTicketMapper;
import com.songshihao.community.dao.UserMapper;
import com.songshihao.community.entity.LoginTicket;
import com.songshihao.community.entity.User;
import com.songshihao.community.util.CommunityConstant;
import com.songshihao.community.util.CommunityUtil;

import com.songshihao.community.util.MailClient;
import com.songshihao.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    // 不推荐使用了
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

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

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        // redis重构
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
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
            // 修改数据后将缓存删除
            clearCache(userId);
            return ACTIVATION_SUEECEE;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 用户的登录逻辑
     * @param username 用户名
     * @param password 用户输入的明文密码
     * @return 返回登陆信息
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 1.空值处理
        // 1.1 验证用户名是否为空
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        // 1.2 验证密码是否为空
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 2.验证登录
        // 2.1 验证账号是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        // 2.2 验证账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号尚未激活");
            return map;
        }
        // 2.3 验证密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 3.生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        // 原先是存在数据库中，现在进行重构，存入到redis中
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    /**
     * 退出登录
     * @param ticket 传入的凭证
     */
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        // redis方法重构
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * 根据ticket查询LoginTicket对象
     * @param ticket ticket 的字符串名称
     * @return LoginTicket 对象
     */
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        // redis方法重构
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);

        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeader(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        // 更新数据,将缓存删除
        clearCache(userId);

        return rows;
    }

    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空");
            return map;
        }

        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误！");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        return map;
    }

    // 根据用户名查询用户
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 主要用于userService内部调用，所以使用private关键字  注解：redis中存入对象会自动转为JSON字符串存储
    // 1. 优先从缓存中取值
    public User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2. 从缓存中取不到值的时候初始化缓存数据（如果能取到就直接返回返回缓存数据）
    public User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 60, TimeUnit.SECONDS);
        return user;
    }

    // 3. 数据变更时清楚缓存数据
    public void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
