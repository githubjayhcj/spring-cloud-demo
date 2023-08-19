package com.example.webService.configure;

import com.example.webService.Filter.TokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: FilterConfigure
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/14 02:34
 * @Version: 1.0.0
 * @Description: TODO
 */
//@Configuration
public class FilterConfigure {

    /*
    * JWT 整合 shiro （前后端分离，token 处理过滤器）
    * */
    //@Bean
    public FilterRegistrationBean JWTfilterRegistrationBean(){
        FilterRegistrationBean<TokenFilter> filterRegistrationBean = new FilterRegistrationBean();
        // 注册过滤器
        filterRegistrationBean.setFilter(new TokenFilter());
        // 设置顺序权重
        filterRegistrationBean.setOrder(1);
        // 具备拦截路径 需要具体路径，非正则匹配
        //filterRegistrationBean.addUrlPatterns("/admin/adminPage");
        return filterRegistrationBean;
    }
}
