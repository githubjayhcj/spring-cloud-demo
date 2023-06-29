package com.example.webService.openFeign;


import com.example.webService.common.DataResult;
import com.example.webService.entity.Product;
import com.example.webService.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(value = "data2-service", fallback = Data2ServiceFallback.class, configuration = Data2ServiceFeignConfiguration.class)
public interface Data2ServiceClient {
    @PostMapping(value = "/saveProduct")
    DataResult insertProduct(@RequestBody Product product);



}

//  configure bean class
class Data2ServiceFeignConfiguration {
    @Bean
    public Data2ServiceFallback data2ServiceFallback(){
        return new Data2ServiceFallback();
    }
}

//  sentinel openfeign fallback class
class Data2ServiceFallback implements Data2ServiceClient{


    @Override
    public DataResult<User> insertProduct(Product product) {
        return new DataResult("Data2ServiceFallback insertProduct 失败 ，请稍后重试。");
    }
}
