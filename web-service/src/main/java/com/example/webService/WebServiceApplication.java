package com.example.webService;

import com.example.webService.entity.Product;
import com.example.webService.entity.SimpleMsg;
import com.example.webService.ioNetty.serverSocket.NettyServerSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
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
// swagger api
public class WebServiceApplication
{
    private static final Logger log = LoggerFactory.getLogger(WebServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebServiceApplication.class, args);
    }

    @Autowired
    public NettyServerSocket nettyServerSocket;
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext applicationContext){
        return args -> {
            System.out.println("commandLineRunner ...."+args);
            System.out.println("commandLineRunner ...."+nettyServerSocket);
            System.out.println("nettyServerSocket start...");
            // 启动 serverSocket
            //nettyServerSocket.start();
            // 监听服务关闭时，启动一个线程主动断开socket服务器
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    System.out.println("Runtime Shutdown...");
                    // close serverSocket/ 关闭 socket 服务
                    nettyServerSocket.getChannelFuture().channel().close();
                }
            });

        };
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
