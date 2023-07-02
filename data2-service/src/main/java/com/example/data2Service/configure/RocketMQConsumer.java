package com.example.data2Service.configure;

import com.example.data2Service.entity.SimpleMsg;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class RocketMQConsumer {

    @Bean
    public Consumer<Message<SimpleMsg>> consumer() {
        return msg -> {
            System.out.println(Thread.currentThread().getName() + " Consumer2 Receive New Messages: " + msg.getPayload().getMsg());
        };
    }
}
