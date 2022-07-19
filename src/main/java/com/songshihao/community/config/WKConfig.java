package com.songshihao.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WKConfig {

    private static final Logger logger = LoggerFactory.getLogger(WKConfig.class);

    // 注入存储路径
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 服务启动的时候检查是否存在wk存储的目录，如果没有则进行自动创建
    @PostConstruct
    public void init() {
        // 创建WK图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            logger.info("创建wk图片目录: " + wkImageStorage);
        }
    }

}
