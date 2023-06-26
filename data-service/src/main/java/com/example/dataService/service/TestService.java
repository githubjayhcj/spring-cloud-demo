package com.example.dataService.service;

import com.example.dataService.entity.User;

import java.io.IOException;
import java.util.List;

public interface TestService {
    int saveService(List<User> user) throws IOException;
}
