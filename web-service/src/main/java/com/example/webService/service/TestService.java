package com.example.webService.service;

import com.example.webService.common.DataResult;
import com.example.webService.entity.Permission;
import com.example.webService.entity.Role;
import com.example.webService.entity.User;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface TestService {
    String resourceA();

    DataResult<User> getUserByName(String name);
    DataResult<List<Role>> getRolesByUid(int id);
    DataResult<List<Permission>> getPermsByUid(int id);
}
