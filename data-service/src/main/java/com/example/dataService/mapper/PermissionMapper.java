package com.example.dataService.mapper;

import com.example.dataService.entity.Permission;
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
public interface PermissionMapper {
    Role selectById(int id);
    List<Permission> getPermsByUid(int id);
}
