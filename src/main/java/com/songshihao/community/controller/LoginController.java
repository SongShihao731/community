package com.songshihao.community.controller;

import com.google.code.kaptcha.Producer;
import com.mysql.cj.util.TimeUtil;
import com.songshihao.community.entity.User;
import com.songshihao.community.service.UserService;
import com.songshihao.community.util.CommunityConstant;
import com.songshihao.community.util.CommunityUtil;
import com.songshihao.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("server.servlet.context-path")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            // 注册成功
            model.addAttribute("msg",
                    "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快查收");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // 要求激活的路径如下: http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUEECEE) {
            model.addAttribute("msg",
                    "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg",
                    "无效的操作，该账号已经激活过！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg",
                    "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // 生成验证码(学完redis之后对其进行重构)
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 根据验证码生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session中(敏感信息且需要多次用到所以存到session中)
        // session.setAttribute("kaptcha", text);

        // 重构，将验证码存入到redis中
        // 1. 验证码的归属，生成一个owner凭证给用户保存到（保存到cookie中）
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 2. 将验证码存入redis中
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);


        // 将图片输出给浏览器
        // 设定输入的格式为
        response.setContentType("image/png");
        // 设定输出流(不用关闭流，springmvc维护)
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("相应验证码失败：", e.getMessage());
        }
    }

    // 用户登录(学完redis对其进行重构)
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session, */HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner) {
        // 1.验证验证码是否正确
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        // 2.验证账号密码是否正确
        int expiredSeconds = rememberme ? DEFAULT_EXPIRED_SECONDS : REMEMBERME_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            // 登录成功
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            // 登录失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // 退出登录
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }
}
