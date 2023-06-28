package com.example.dataService.service;


import com.example.dataService.common.DataResult;
import com.example.dataService.entity.User;

import java.io.IOException;
import java.util.List;

public interface TestService {
    DataResult saveService(List<User> user);
    int saveService2(List<User> user) throws Exception;
}
