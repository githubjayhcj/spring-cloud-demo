package com.example.data2Service.serviceImpl;

import com.example.data2Service.common.DataResult;
import com.example.data2Service.entity.Product;
import com.example.data2Service.mapper.ProductMapper;
import com.example.data2Service.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Value("${server.port}")
    private int serverPort;

    @Value("${spring.application.name}")
    private String serverName;

    @Autowired
    private ProductMapper productMapper;


    @Override
    public DataResult insertProduct(Product product) {
        int save = productMapper.insertProduct(product);
        DataResult dataResult = new DataResult();
        dataResult.setMessage("save ok");
        return dataResult;
    }
}
