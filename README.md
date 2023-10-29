# spring-cloud-demo

JDK 版本：20.0.1 ；
该项目为自行搭建的 Spring Cloud Alibaba 2022.0.0.0-RC2* 最新毕业版本。
基于：Spring Cloud 2022.0.0、Spring Boot 3.0.2
数据库：mysql 8.0
服务代理：nginx  负载均衡配置
maven  父子项目；
项目集成内容：
Spring Data JPA；Spring Data JDBC：mybatis(annotation + xml)；
Spring data Redis (lettuce) key-value(noSql db) 数据库 临时存储;
Spring data elasticsearch  索引搜索引擎：stringQuery (搜索);
Spring Session：httpSession  整合 Spring Data Redis  实现；
Spring Cloud OpenFeign ：微服务接口调用及负载均衡。负载均衡由 Spring Cloud Loadbalancer nacos  实现；
Spring Cloud Stream：RocketMQ Binder Alibaba  实现的事件驱动 （Event
-driven）。项目中已实现的主题（topic ）为广播模式（broadcast）、顺序消费模式（orderly）；
Shiro : 账号登录认证及用户权限授权 ;
SpringDoc ：类 Swagger-ui 接口管理框架 ；
Alibaba Nacos ：服务注册、服务探测、配置服务器；
Alibaba Sentinel （适配了 OpenFeign  组件）：资源定义、流量控制管理、服务熔断降级处理；
Alibaba Seata ：分布式事务管理；
视图技术：Spring Web MVC  中的 View Technologies -Thymeleaf ，spring-boot-starter-thymeleaf  实现 ;
JWT ( json-web-token ) : 整合 shiro  配合 Filter  做 noSession  前后端分离权限验证 ；
网络编程：基于nio socket 非阻塞式网络通信框架 io-netty（netty-all,version:4.1.92.Final） 的实现demo （项目部署阿里云，访问地址：http://47.99.139.38:8080/#/netty-socket ）
Alipay（阿里支付）：基于沙箱的web网页支付。