package com.example.webService.pageController;

import com.example.webService.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {

    @RequestMapping("/home")
    public String home(Model model) throws Exception{
        model.addAttribute("say","hello thymeleaf!");
        return "home";
    }

    @RequestMapping("/registerPage")
    public String register(Model model) throws Exception{

        return "/register";
    }

    @RequestMapping("/loginPage")
    public String login(Model model) throws Exception{

        return "login";
    }

    @RequestMapping("/commonError")
    public String error(Model model) throws Exception{

        return "/error";
    }

//    @RequiresRoles("manager")
    @RequiresPermissions("add")
    @RequestMapping("/admin/index")
    public String index(Model model, HttpServletRequest request) throws Exception{
        System.out.println("index page ...");

        Subject subject = SecurityUtils.getSubject();

        System.out.println("isAuthenticated:"+subject.isAuthenticated());
        System.out.println("hasRole:"+subject.hasRole("manager"));

        model.addAttribute("say","hello index!");
        return "/admin/index";

    }
}
