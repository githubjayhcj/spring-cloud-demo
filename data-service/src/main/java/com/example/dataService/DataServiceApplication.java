package com.example.dataService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataServiceApplication.class, args);
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
