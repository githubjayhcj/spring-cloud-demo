package com.example.dataService;

import com.example.dataService.entity.SimpleMsg;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@SpringBootApplication
// nacos client
@EnableDiscoveryClient
// openFeign
@EnableFeignClients
public class DataServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(DataServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DataServiceApplication.class, args);
	}

	@Bean
	public Consumer<Message<SimpleMsg>> consumer() {
		return msg -> {
			log.info(Thread.currentThread().getName() + " Consumer1 Receive New Messages: " + msg.getPayload().getMsg());
		};
	}

	//	public static void main(String[] args) {
//		ConfigurableApplicationContext applicationContext = SpringApplication.run(DataServiceApplication.class, args);
//		while(true) {
//			//当动态配置刷新时，会更新到 Enviroment中，因此这里每隔一秒中从Enviroment中获取配置
//			String userAge = applicationContext.getEnvironment().getProperty("useLocalCache");
//			System.err.println("applicationContext :" + applicationContext);
//			System.err.println("getEnvironment :" + applicationContext.getEnvironment());
//			System.err.println("useLocalCache :" + "; age: " + userAge);
//			try {
//				TimeUnit.SECONDS.sleep(1);
//			}catch (Exception e){
//				System.err.println("DataServiceApplication main :"+e.getMessage());
//			}
//
//		}
//	}

}
