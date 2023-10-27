package com.example.webService.Filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.webService.common.DataResult;
import com.example.webService.utils.JWTutils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @ClassName: JWTfilter
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/14 02:29
 * @Version: 1.0.0
 * @Description: JWT 整合 shiro （前后端分离，token 处理过滤器）
 */

@Component  //也可利用spring filterRegisterBean 注册
//
@WebFilter
@Order(value = 1)
public class TokenFilter implements Filter {

    // 需要登录的请求路径
    private List<String> admins = new ArrayList<>(Arrays.asList(new String[]{
            "/admin/**"
    }));

    // spring url pattern 类
    AntPathMatcher requestPattern = new AntPathMatcher();

    @Autowired
    private JWTutils jwTutils;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        System.out.println("TokenFilter init...");

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
            // 需要权限的匹配路径
            String accordUrl = "";
            // 当前请求路径
            String requestUrl = "";
            //
            //System.out.println("request url: "+req.getServletPath());
            requestUrl = req.getRequestURI();
            System.out.println("request path: "+requestUrl);
            //
            for (String adminUrl : admins){
                // 匹配需要权限路径
                if(requestPattern.match(adminUrl,requestUrl)){
                    //
                    accordUrl = adminUrl;
                    break;
                }
            }
            //
            if (!accordUrl.equals("")){ // need auth
                //
                System.out.println(requestUrl+"请求匹配"+accordUrl+"权限路由,该请求需要登录权限。");
                req.setAttribute("dataResult",new DataResult<String>(requestUrl+"请求匹配"+accordUrl+"权限路由,该请求需要登录权限。",requestUrl+"请求匹配"+accordUrl+"权限路由,该请求需要登录权限。"));
                req.getRequestDispatcher("/systemFeedBack").forward(req,rep);
            }else {// none need auth
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
