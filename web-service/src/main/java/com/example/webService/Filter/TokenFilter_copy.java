package com.example.webService.Filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.webService.common.DataResult;
import com.example.webService.utils.JWTutils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

/**
 * @ClassName: JWTfilter
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/14 02:29
 * @Version: 1.0.0
 * @Description: JWT 整合 shiro （前后端分离，token 处理过滤器）
 */

//@Component  //也可利用spring filterRegisterBean 注册
//@WebFilter
//@Order(value = 1)
public class TokenFilter_copy implements Filter {

    // 需要登录的请求路径
    private Map<String,List<String>> adminsMap = new HashMap<>();
    private List<String> admins = new ArrayList<>(Arrays.asList(new String[]{
            "/admin/**"
    }));
    /*
    * 如下为权限路径定制的匹配规则:
    * */
//    private List<String> admins = new ArrayList<>(Arrays.asList(new String[]{
//            "/**",
//            "/*",
//            "/admin/**",
//            "/admin/*",
//            "/admin/main",
//            "/admin/main/page",
//            "/admin/main/*",
//            "/admin/main/**",
//            "/admin/main/page/*",
//            "/admin/main/page/**"
//    }));

    @Autowired
    private JWTutils jwTutils;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        System.out.println("TokenFilter init...");
        // 初始化需要登录的请求路径
        Iterator iterator = admins.iterator();
        while (iterator.hasNext()){
            String adminPattern = (String) iterator.next();
            System.out.println("adminPattern:"+adminPattern);
            //
            this.adminsMap.put(adminPattern,Arrays.asList(adminPattern.split("/")));
            // 验证格式是否正确 ： /** , /*
            //String pattern = "^/[*]{1,2}$";

            // 验证格式是否正确 ： /admin/** , "/admin/*"
            //String pattern = "^/[A-Za-z0-9_]+/([*]{1,2})$";

            // 验证格式是否正确 ： /admin/main , /admin/main/page
            //String pattern = "^/[A-Za-z0-9_]+(/[A-Za-z0-9_]+)*";

            // 验证格式是否正确 ： /admin/** , "/admin/*" , /admin/main , /admin/main/page , /admin/main/* ,  /admin/main/**  ,  /admin/main/page/*  ,  /admin/main/page/**
            //String pattern = "^/[A-Za-z0-9_]+(/[A-Za-z0-9_]+)*(/[*]{1,2})?$";

            String pattern = "(^/[*]{1,2}$)|(^/[A-Za-z0-9_]+(/[A-Za-z0-9_]+)*(/[*]{1,2})?$)";
            if(!adminPattern.matches(pattern)){
                //
                throw new ExceptionInInitializerError("权限正则匹配错误："+adminPattern);
            }
        }
        System.out.println("adminsMap init..."+adminsMap.toString());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("TokenFilter doFilter   before...");
        //
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rep = (HttpServletResponse) response;
        //
        String token = req.getHeader("Authorization");
        System.out.println("do filter token:"+token);
        if(token != null && token != ""){
            token = token.substring(new String("Bearer").length()+1);
            System.out.println("token:"+token);
            // 执行成功无异常则为有效token
            try {
                //
                DecodedJWT decodedJWT = jwTutils.decodeAndVerifyToken(token);
                System.out.println("decodedJWT name:"+decodedJWT.getClaim("name"));
                //
                UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(token,token);
                Subject subject = SecurityUtils.getSubject();
                subject.login(usernamePasswordToken);
                req.setAttribute("subject",subject);
                System.out.println("subject?:"+subject);
                subject.login(usernamePasswordToken);
                //
                System.out.println("is login?:"+subject.isAuthenticated());
                System.out.println("is permission?:"+subject.isPermitted("add"));
                //
                req.setAttribute("token",token);
                //req.setAttribute("upToken",token);
                chain.doFilter(request,response);
            }catch (JWTVerificationException e){
                System.out.println("JWTVerificationException...");
                req.setAttribute("dataResult",new DataResult<String>("JWTVerificationException : token 验证无效！","JWTVerificationException : token 验证无效！"));
                req.getRequestDispatcher("/systemFeedBack").forward(req,rep);
            }
        }else {// 需要登录权限的请求
            // 匹配的拦截验证
            String rexPathPattern = "";
            // 当前请求路径
            String requestUrl = "";
            //
            System.out.println("request url: "+req.getServletPath());
            requestUrl = req.getRequestURI();
            System.out.println("request path: "+requestUrl);

            //
            List<String> requestPaths = Arrays.asList(requestUrl.split("/"));
            System.out.println("requestPaths: "+requestPaths);
            //
            for (Map.Entry<String,List<String>> entryAdminPatter : this.adminsMap.entrySet()){
                System.out.println("entryAdminPatter:"+entryAdminPatter.toString());
                // /**
                if(entryAdminPatter.getKey().equals("/**")){
                    rexPathPattern = entryAdminPatter.getKey();
                    break;
                }
                // /*
                if(entryAdminPatter.getKey().equals("/*")){
                    if(requestUrl.matches("^/[A-Za-z0-9_?=]+")){
                        rexPathPattern = entryAdminPatter.getKey();
                        break;
                    }
                }
                // 以 /admin 开头的请求
                List<String> paths = entryAdminPatter.getValue();
                //
                label:for(int i=1;i<paths.size();i++){
                    System.out.println("paths index:"+i);
                    // 路径对位匹配，或以 /** 结尾
                    if(paths.get(i).equals("**")){
                        if(requestPaths.size() >= (i+1)){
                            rexPathPattern = entryAdminPatter.getKey();
                            break;
                        }
                    }
                    // 路径对位匹配，或以 /** 结尾
                    if(paths.get(i).equals("*")){
                        if(requestPaths.size() == (i+1)){
                            rexPathPattern = entryAdminPatter.getKey();
                            break;
                        }
                    }
                    // 路径对位匹配，每一级，如 /admin/main
                    if(i < requestPaths.size()){
                        if(paths.get(i).equals(requestPaths.get(i))){
                            //
                            if(paths.get(i+1) == null && requestPaths.get(i+1) == null){
                                rexPathPattern = entryAdminPatter.getKey();
                                break;
                            }
                            continue label;
                        }else {
                            break;
                        }
                    }
                }
            }
            //
            if(!rexPathPattern.isEmpty()){
                System.out.println(requestUrl+"请求匹配"+rexPathPattern+"权限路由,该请求需要登录权限。");
                req.setAttribute("dataResult",new DataResult<String>(requestUrl+"请求匹配"+rexPathPattern+"权限路由,该请求需要登录权限。",requestUrl+"请求匹配"+rexPathPattern+"权限路由,该请求需要登录权限。"));
                req.getRequestDispatcher("/systemFeedBack").forward(req,rep);
            }else{
                //
                chain.doFilter(request,response);
            }
        }
        //
        System.out.println("TokenFilter doFilter   after...");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
        System.out.println("TokenFilter destroy...");
    }
}
