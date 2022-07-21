package com.songshihao.community.service;

import com.songshihao.community.dao.AlphaDao;
import com.songshihao.community.dao.DiscussPostMapper;
import com.songshihao.community.dao.UserMapper;
import com.songshihao.community.entity.DiscussPost;
import com.songshihao.community.entity.User;
import com.songshihao.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // 无参构造器
    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    // 这个方法将在构造之后执行
    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    // 在销毁之前进行调用
    @PreDestroy
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    // 模拟一个业务方法，去调用alphaDao
    public String find() {
        return alphaDao.select();
    }

    // 模拟一个业务方法来进行事务的管理
    // Transactional 注解告诉spring这是个事务
    // isolation 选择隔离等级
    // propagation传播机制(假设情境：A调用B)
    // REQUIRED:支持当前事务（A就是当前事务，外部事务），如果外部事务不存在，则创建新事务
    // REQUITED_NEW：创建一个新事务（B），并且暂停当前事务（A）
    // NESTED：如果存在当前事务（即外部事务A），则嵌套在该事务中执行（B嵌套在A中，B有独立的提交和回滚），否则就和REQUIRED
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello!");
        post.setContent("新人报道！");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        // 人为造错，观察事务回滚
        Integer.valueOf("abc");

        return "ok";
    }

    // 模拟一个业务方法模拟编程式事务管理
    public Object save2() {
        // 设置隔离等级
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        // 设置传播等级
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好!");
                post.setContent("我是新人！");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                // 人为造错，观察事务回滚
                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

    // 让该方法在多线程环境下，被异步地调用
    @Async
    public void execute1() {
        logger.debug("execute1");
    }

//    @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    public void execute2() {
        logger.debug("execute2");
    }
}
