package com.songshihao.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    // 设置发件人
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件的方法体
     *
     * @param to      发送邮件的目标
     * @param subject 发送的主题是
     * @param content 发送的内容
     */
    public void sendMail(String to, String subject, String content) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 设置发件人
            helper.setFrom(from);

            // 设置收件人
            helper.setTo(to);

            // 设置邮件主题
            helper.setSubject(subject);

            // 设置邮件的内容,支持HTML文本
            helper.setText(content, true);

            // 发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
        }
    }
}
