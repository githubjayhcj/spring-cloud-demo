package com.example.dataService.mapper;


import com.example.dataService.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from user where id = #{id}")
    public User getById(@Param("id") int id);

    public User selectById(int id);
    public User selectByName(String name);

    int insertUser(User user);
}
