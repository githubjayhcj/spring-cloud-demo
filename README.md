# spring-cloud-demo

基于：Spring Cloud 2022.0.0、Spring Boot 3.0.2
数据库：mysql 8.0
服务代理：nginx 负载均衡配置
maven 父子项目；
项目集成内容：
Spring Data JPA；Spring Data JDBC：mybatis(annotation + xml)；
Spring Session：httpSession 整合 Spring Data Redis 实现；
Spring Cloud OpenFeign：微服务接口调用及负载均衡。负载均衡由Spring Cloud Loadbalancer nacos实现；
Spring Cloud Stream：RocketMQ Binder Alibaba实现的事件驱动 （Event-driven）。项目中已实现的主题（topic）为广播模式（broadcast）、顺序消费模式（orderly）；
Alibaba Nacos ：服务注册、服务探测、配置服务器；
Alibaba Sentinel（适配了OpenFeign 组件）：资源定义、流量控制管理、服务熔断降级处理；
Alibaba Seata：分布式事务管理；
视图技术：Spring Web MVC 中的View Technologies - Thymeleaf ，spring-boot-starter-thymeleaf实现
