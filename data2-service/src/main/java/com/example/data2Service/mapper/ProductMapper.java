package com.example.data2Service.mapper;


import com.example.data2Service.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {
    Product selectById(int id);
    int insertProduct(Product product);
}
