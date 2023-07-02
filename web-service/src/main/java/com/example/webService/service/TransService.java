package com.example.webService.service;

import com.example.webService.common.DataResult;
import com.example.webService.entity.Product;
import com.example.webService.entity.User;

import java.util.List;

public interface TransService {

    List<DataResult> transSave(User user, Product product) throws Exception;
}
