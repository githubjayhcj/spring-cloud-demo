package com.example.data2Service;

import com.example.data2Service.entity.SimpleMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Data2ServiceApplication
{
    public static void main(String[] args) {
        SpringApplication.run(Data2ServiceApplication.class, args);
    }

    private static final Logger log = LoggerFactory
            .getLogger(Data2ServiceApplication.class);

//    @Bean
//    public Consumer<Message<SimpleMsg>> consumer() {
//        return msg -> {
//            log.info(Thread.currentThread().getName() + " Consumer2 Receive New Messages: " + msg.getPayload().getMsg());
//        };
//    }
}
