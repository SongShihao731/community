package com.songshihao.community;

import com.songshihao.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    // 不用通过路径地址来进行测试
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Test
    public void testTextMail() {
        mailClient.sendMail("532509182@qq.com", "TEST", "Welcome!");
    }

    @Test
    public void testHtmlMail() {
        // 通过context构建参数
        Context context = new Context();
        context.setVariable("username", "sunday");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        // 发送邮件
        mailClient.sendMail("532509182@qq.com", "HTML", content);
    }
}
