package com.example.webService.pageController;

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

    @RequestMapping("/index")
    public String index(Model model) throws Exception{
        model.addAttribute("say","hello index!");
        return "other/index";
    }
}
