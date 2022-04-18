package com.songshihao.community.service;

import com.songshihao.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

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
}
