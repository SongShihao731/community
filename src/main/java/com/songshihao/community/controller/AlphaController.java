package com.songshihao.community.controller;

import com.songshihao.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot!";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 1.获取请求数据
        System.out.println(request.getMethod()); // 获取请求方式
        System.out.println(request.getServletPath()); // 获取请求路径  // 请求行第一行的数据
        Enumeration<String> enumeration = request.getHeaderNames(); // 获取请求头的key迭代器  // 请求行若干行的数据
        // 迭代器进行迭代
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ":" + value);
        }
        // 获取请求体名为code的参数
        System.out.println(request.getParameter("code"));

        // 2.返回响应数据
        response.setContentType("text/html;charset=utf-8");  // 返回的类型为html网页，字符编码为utf-8
        try (
                PrintWriter writer = response.getWriter() // 往网页上打印数据  // 放在括号中为释放流
        ) {
            writer.write("<h1>欢迎来到首页~</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GET请求

    // 情境1： /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody // 返回一个简单的字符串
    public String getStudents(@RequestParam(name = "current", required = false, defaultValue = "1") int current,
                              @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {

        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // 情境2： /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "A Student";
    }

    // POST请求
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, String age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 响应HTML数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", 30);
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", 80);
        return "/demo/view";
    }

    // 响应JSON数据（异步请求）
    // 会自动将Map集合转化为JSON字符串
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 23);
        map.put("salary", 8000.00);
        return map;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 23);
        map.put("salary", 8000.00);
        list.add(map);

        map = new HashMap<>();
        map.put("name", "李四");
        map.put("age", 24);
        map.put("salary", 9000.00);
        list.add(map);


        map = new HashMap<>();
        map.put("name", "王五");
        map.put("age", 25);
        map.put("salary", 10000.00);
        list.add(map);

        return list;
    }
}
