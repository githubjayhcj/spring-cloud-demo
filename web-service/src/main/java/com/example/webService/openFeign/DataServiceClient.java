package com.example.webService.openFeign;


import com.example.webService.common.DataResult;
import com.example.webService.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient("data-service")
public interface DataServiceClient {
    @RequestMapping(value = "/getUserById/{id}")
    DataResult<User> getUserById(@PathVariable int id);

    @PostMapping(value = "/saveUser")
    DataResult<User> saveUser(@RequestBody User user);
}
