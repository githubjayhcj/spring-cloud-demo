package com.example.dataService.serviceImpl;

import com.example.dataService.entity.User;
import com.example.dataService.mapper.UserMapper;
import com.example.dataService.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public int saveService(List<User> users) throws IOException {
        System.out.println("all user =======");
        System.out.println(users.get(0).toString());
        System.out.println(users.get(1).toString());

        for (int i = 0;i<2;i++){
            int save1 = userMapper.insertUser(users.get(i));
            System.out.println("save"+i+"="+save1);

        }
        return 0;
    }
}
