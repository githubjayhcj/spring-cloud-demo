# spring-cloud-demo

JDK 版本：20.0.1 ；
基于：Spring Cloud 2022.0.0、Spring Boot 3.0.2
数据库：mysql 8.0
服务代理：nginx 负载均衡配置
maven 父子项目；
项目集成内容：
Spring Data JPA；Spring Data JDBC：mybatis(annotation + xml)；
Spring data Redis (lettuce)  key-value(no sql db) 数据库 临时存储；
Spring data elasticsearch  索引搜索引擎：stringQuery(搜索)；
Spring Session：httpSession 整合 Spring Data Redis 实现；
Spring Cloud OpenFeign：微服务接口调用及负载均衡。负载均衡由Spring Cloud Loadbalancer nacos实现；
Spring Cloud Stream：RocketMQ Binder Alibaba实现的事件驱动 （Event-driven）。项目中已实现的主题（topic）为广播模式（broadcast）、顺序消费模式（orderly）；
Shiro ：用户登录验证、权限授权管理 框架;
Alibaba Nacos ：服务注册、服务探测、配置服务器；
Alibaba Sentinel（适配了OpenFeign 组件）：资源定义、流量控制管理、服务熔断降级处理；
Alibaba Seata：分布式事务管理；
视图技术：Spring Web MVC 中的View Technologies - Thymeleaf ，spring-boot-starter-thymeleaf实现
*网络编程:io-netty 框架 main 方法测试；
