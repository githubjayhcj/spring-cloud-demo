package com.example.dataService.configure;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        System.err.println("openFeign requestTemplate:"+requestTemplate.toString());
        System.err.println("全局事务，XID = " + RootContext.getXID());

        String xid = RootContext.getXID();
        if (StringUtils.isNotEmpty(xid)) {
            requestTemplate.header(RootContext.KEY_XID, xid);
        }
        System.err.println("set after openFeign requestTemplate:"+requestTemplate.toString());
    }
}
