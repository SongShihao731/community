package com.songshihao.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {
        // 封装properties文件的key value
        Properties properties = new Properties();
        // 图片长宽
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        // 字体大小和颜色
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 验证码生成的范围和大小
        properties.setProperty("kaptcha.textproducer.char.string",
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码噪声的类型
        properties.setProperty("kaptcha.noise.impl",
                "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
