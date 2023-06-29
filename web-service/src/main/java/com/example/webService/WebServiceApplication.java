package com.example.webService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Hello world!
 *
 */
@SpringBootApplication
// nacos client
@EnableDiscoveryClient
// spring httpSession
@EnableRedisHttpSession
// openFeign
@EnableFeignClients
public class WebServiceApplication
{
    public static void main(String[] args) {
        SpringApplication.run(WebServiceApplication.class, args);
    }

}
