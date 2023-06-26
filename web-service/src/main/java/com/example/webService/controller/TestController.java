package com.example.webService.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${server.port}")
    private String serverPort;

    @RequestMapping("/test")
    public String test(HttpServletRequest request) {

        HttpSession session = request.getSession();
        if (session.getAttribute("port") == null){
            session.setAttribute("port",serverPort);
        }else {
            System.err.println("session port ="+session.getAttribute("port"));
        }
        return "--------serverPort="+serverPort+"::session store port="+session.getAttribute("port");
    }

    @RequestMapping("/setSession/{value}")
    public String set(HttpServletRequest request, @PathVariable String value) {
        HttpSession session = request.getSession();
        session.setAttribute("sessionVal",value);
        return serverPort+"ok";
    }

    @RequestMapping("/getSession")
    public String get(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionValue = (String) session.getAttribute("sessionVal");
        return serverPort+":sessionValue="+sessionValue;
    }

}
