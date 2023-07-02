package com.example.dataService.openFeign;



import com.example.dataService.common.DataResult;
import com.example.dataService.entity.Product;
import com.example.dataService.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@FeignClient(value = "data2-service")
public interface Data2ServiceClient {
    @PostMapping(value = "/saveProduct")
    DataResult insertProduct(Product product);

    @GetMapping("/getPById/{id}")
    DataResult getPById(@PathVariable int id);

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
        return new DataResult(0,"Data2ServiceFallback insertProduct 失败 ，请稍后重试。");
    }

    @Override
    public DataResult getPById(int id) {
        return new DataResult(0,"Data2ServiceFallback getPById 失败 ，请稍后重试。");
    }
}
