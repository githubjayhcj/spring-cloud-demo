package com.example.data2Service.controller;

import com.example.data2Service.common.DataResult;
import com.example.data2Service.entity.Product;
import com.example.data2Service.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private TestService testService;


    @GetMapping("/saveP")
    public DataResult saveP(){

        Product product = new Product("product 1",123);
        DataResult dataResult = testService.insertProduct(product);
        dataResult.setMessage("save ok");
        return dataResult;
    }

    @PostMapping("/saveProduct")
    public DataResult insertProduct(@RequestBody Product product){

        DataResult dataResult = testService.insertProduct(product);
        dataResult.setMessage("data2service save ok");
        return dataResult;
    }
}
