package com.example.data2Service.service;



import com.example.data2Service.common.DataResult;
import com.example.data2Service.entity.Product;

import java.util.List;

public interface TestService {
    DataResult insertProduct(Product product);
}
