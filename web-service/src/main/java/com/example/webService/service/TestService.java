package com.example.webService.service;

import com.example.webService.common.DataResult;
import com.example.webService.entity.User;

public interface TestService {
    String resourceA();

    DataResult<User> getUserByName(String name);
}
