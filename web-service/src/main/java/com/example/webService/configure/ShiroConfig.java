package com.example.webService.configure;

import com.example.webService.shiroRealm.DataBaseRealm;
import kotlin.PublishedApi;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // 非官方 shiro-spring-boot-web-start 配置方式
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // set securityManager
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);

        /*
        认证过滤器
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
        Map<String,String> map = new HashMap<>();
        // 无需登录地址
        map.put("/**","anon");
        // 设置需要登录的url
        map.put("/admin/**","authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);

        // 设置 跳转登录页
        shiroFilterFactoryBean.setLoginUrl("/loginPage");
        // 设置未授权跳转页面
        shiroFilterFactoryBean.setUnauthorizedUrl("/error.html");

        return shiroFilterFactoryBean;
    }

    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(RememberMeManager rememberMeManager){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(dataBaseRealm);
        // 设置 rememberMe cookie
        defaultWebSecurityManager.setRememberMeManager(rememberMeManager);

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
