package com.example.webService.openFeign;


import com.example.webService.common.DataResult;
import com.example.webService.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(value = "data-service", fallback = DataServiceFallback.class, configuration = FeignConfiguration.class)
public interface DataServiceClient {
    @RequestMapping(value = "/getUserById/{id}")
    DataResult<User> getUserById(@PathVariable int id);

    @PostMapping(value = "/saveUser")
    DataResult<User> saveUser(@RequestBody User user);

    @PostMapping(value = "/erro")
    DataResult erro();


}

//  configure bean class
class FeignConfiguration {
    @Bean
    public DataServiceFallback dataServiceFallback(){
        return new DataServiceFallback();
    }
}

//  sentinel openfeign fallback class
class DataServiceFallback implements DataServiceClient{

    @Override
    public DataResult<User> getUserById(int id) {
        return new DataResult<>(0,"getUserById 失败 ，请稍后重试。");
    }

    @Override
    public DataResult<User> saveUser(User user) {
        return new DataResult<>(0,"saveUser 失败 ，请稍后重试。");
    }

    @Override
    public DataResult erro() {
        return new DataResult<>(0,"erro 失败 ，请稍后重试。");
    }
}
