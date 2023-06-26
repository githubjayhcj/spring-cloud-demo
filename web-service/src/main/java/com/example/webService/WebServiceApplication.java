package com.example.webService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableRedisHttpSession
public class WebServiceApplication
{
    public static void main(String[] args) {
        SpringApplication.run(WebServiceApplication.class, args);
    }
}
