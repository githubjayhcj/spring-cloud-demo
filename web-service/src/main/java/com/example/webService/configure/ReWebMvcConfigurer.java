package com.example.webService.configure;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName: ReWebMvcConfigurer
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/12 05:41
 * @Version: 1.0.0
 * @Description: TODO
 */
@Configuration
public class ReWebMvcConfigurer implements WebMvcConfigurer {

    // 允许 跨域请求携带 cookie （添加允许携带cookie 接入的域名白名单）
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        WebMvcConfigurer.super.addCorsMappings(registry);
//        //
//        registry.addMapping("/**")//允许所有映射
//                .allowedHeaders("*")//允许所有头
//                .allowedMethods("*")//允许所有方法
//                .allowedOrigins("http://localhost:5173","http://localhost:8084")//如果开启了允许携带cookie，则这里必须这样写多个允许接入的域名加端口(类似白名单)，不能用*（否则任何网站都能接入，存在安全问题）
//                .allowedOriginPatterns("/*") //
//                .allowCredentials(true);//开启允许携带cookie
//    }

}
