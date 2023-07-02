package com.example.webService;

import com.example.webService.entity.Product;
import com.example.webService.entity.SimpleMsg;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.HashMap;
import java.util.Map;

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
    private static final Logger log = LoggerFactory.getLogger(WebServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebServiceApplication.class, args);
    }


    @Autowired
    private StreamBridge streamBridge;

//    @Bean
//    public ApplicationRunner producer() {
//        return args -> {
//            for (int i = 0; i < 100; i++) {
//                String key = "KEY" + i;
//                Map<String, Object> headers = new HashMap<>();
//                headers.put(MessageConst.PROPERTY_KEYS, key);
//                headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);
//                Message<SimpleMsg> msg = new GenericMessage<SimpleMsg>(new SimpleMsg("Hello RocketMQ " + i), headers);
//                streamBridge.send("producer-out-0", msg);
//            }
//        };
//    }

}
