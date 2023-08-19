package com.example.webService.configure;

import com.example.webService.shiroRealm.DataBaseRealm;
import com.example.webService.shiroRealm.DataBaseRealmByJWT;
import kotlin.PublishedApi;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.*;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: shiroConfig
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/7/29 19:05
 * @Version: 1.0.0
 * @Description: TODO
 */

@Configuration
public class ShiroConfig {

    @Autowired
    private DataBaseRealm dataBaseRealm;

    // JWT 整合 shiro 的验证 realm
    @Autowired
    private DataBaseRealmByJWT dataBaseRealmByJWT;

    // 非官方 shiro-spring-boot-web-start 配置方式
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // set securityManager
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);

        /*
        设置“总的”权限路径，先验证过滤器路径所需条件，再验证 controller 所需 role 和 permission

        认证过滤器  (以下名称为 Filter map 的 key 值)
        anon：无需认证。
        authc：必须认证。
        authcBasic：需要通过HTTPBasic认证。
        user：不一定通过认证，只要曾经被Shiro记录即可，比如：记住我。

        授权过滤器
        perms：必须拥有某个权限才能访问。
        role：必须拥有某个角色才能访问。
        port：请求的端口必须是指定值才可以。
        rest：请求必须基于RESTful，POST，PUT，GET，DELETE。
        ssl：必须是安全的URL请求，协议HTTP。
        */
        Map<String,String> filterUrlMap = new HashMap<>();
        // 无需登录地址
        filterUrlMap.put("/**","anon");  // 前后端分离因为要返回提示信息需要 filter 中自行定义 提示信息转发（shiro 原生过滤器为重定向response.redirect）
        // 设置需要登录的url （该路径下需要用户处于登录状态 （subject.isAuthentication）,否则跳转登录页 ）
        //filterUrlMap.put("/admin/**","authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterUrlMap);

        // 设置 无登录状态，跳转登录页（任意地址）「 存疑：仅针对过滤器验证跳转 」
        //shiroFilterFactoryBean.setLoginUrl("/loginPage");
        //shiroFilterFactoryBean.setLoginUrl("/noAuthentication");
        // 设置无授权跳转页面（地址）「 存疑：仅针对过滤器验证跳转 」
        //shiroFilterFactoryBean.setUnauthorizedUrl("/error.html");
        // 关键：全局配置NoSessionCreationFilter，把整个项目切换成无状态服务 (禁用session 状态保持交给 JWT)。
//        shiroFilterFactoryBean.setGlobalFilters(Arrays.asList("noSessionCreation"));

        return shiroFilterFactoryBean;
    }

    // 禁用session 状态保持交给 JWT
    @Bean
    public SessionStorageEvaluator sessionStorageEvaluator(){
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        return defaultSessionStorageEvaluator;
    }

    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(RememberMeManager rememberMeManager){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        // 设置 realms(多域) 验证策略
        ModularRealmAuthenticator modularRealmAuthenticator = new ModularRealmAuthenticator();// 该验证对象（Authenticator ）负责调用 SecurityManager 中realm 集合的验证。
        modularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        defaultWebSecurityManager.setAuthenticator(modularRealmAuthenticator);// 一个realm 验证成功，则成功
        // 设置权限验证 “域”-realm ，可多个，通过配置验证策略来使用。
        //defaultWebSecurityManager.setRealm(dataBaseRealm); // 前后端一体项目验证
        // JWT 整合 shiro 的验证 realm
        defaultWebSecurityManager.setRealm(dataBaseRealmByJWT);// 前后端分离项目验证 JWT token
        // 设置 rememberMe cookie
        defaultWebSecurityManager.setRememberMeManager(rememberMeManager);
        // 关键：全局配置NoSessionCreationFilter，把整个项目切换成无状态服务 (禁用session 状态保持交给 JWT)。
        DefaultSubjectDAO defaultSubjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        defaultSubjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        defaultWebSecurityManager.setSubjectDAO(defaultSubjectDAO);

        // 手动绑定SecurityManager 对象到 shiro 线程中， filter 中 SecurityUtils.getSubject(); 获取对象时会用到
        ThreadContext.bind(defaultWebSecurityManager);

        return defaultWebSecurityManager;
    }

    // 设置 rememberMe cookie
    @Bean
    public RememberMeManager rememberMeManager(SimpleCookie simpleCookie){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        //
        cookieRememberMeManager.setCookie(simpleCookie);
        return cookieRememberMeManager;
    }

    @Bean
    public SimpleCookie simpleCookie(){
        //
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");//cookie 名称
        // 设置跨域
        //simpleCookie.setDomain();
        // 设置域名范围
        simpleCookie.setPath("/");
        // 防止某些攻击，不被Javascript代码获取
        simpleCookie.setHttpOnly(true);
        // 设置有效期,单位为秒
        simpleCookie.setMaxAge(60);

        return simpleCookie;
    }

    // 开启shiro aop注解支持.
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager defaultWebSecurityManager){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        // set SecurityManager
        authorizationAttributeSourceAdvisor.setSecurityManager(defaultWebSecurityManager);
        return authorizationAttributeSourceAdvisor;
    }



}
