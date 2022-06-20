package com.songshihao.community.config;

import com.songshihao.community.util.CommunityConstant;
import com.songshihao.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略静态资源的拦截
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 进行授权
        http.authorizeRequests()
                .antMatchers(
                        // 需要授权才能访问的路径
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        // 授权哪些用户有权限可以登录以上网页
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        // 置顶
                        "/discuss/top",
                        // 加精
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        // 删除
                        "/discuss/delete"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                // 其他网页不需要权限
                .anyRequest().permitAll()
                // 因为太过麻烦，所以不启用csrf防攻击
                .and().csrf().disable();

        // 权限不够时如何处理
        http.exceptionHandling()
                // 用户未登录的时候如何处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 判断请求是普通请求还是异步请求，普通请求则返回登录页面，异步请求返回JSON字符串进行提示
                        // 判断请求的类型
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            // 异步请求
                            // 声明返回的数据类型（普通字符串）
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您还没有登录！"));
                        }else {
                            // 非异步请求（普通请求）
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                // 登录了但是权限不足时应该如何处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        // 判断请求是普通请求还是异步请求，普通请求则返回登录页面，异步请求返回JSON字符串进行提示
                        // 判断请求的类型
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            // 异步请求
                            // 声明返回的数据类型（普通字符串）
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您没有访问此功能的权限！"));
                        }else {
                            // 非异步请求（普通请求）
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的代理,才能执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout");
    }
}
