package com.example.dataService.mapper;

import com.example.dataService.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @ClassName: RoleMapper
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/1 12:36
 * @Version: 1.0.0
 * @Description: TODO
 */
@Mapper
public interface RoleMapper {
    Role selectById(int id);
    List<Role> getRoleByUid(int id);
}
