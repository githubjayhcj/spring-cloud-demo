package com.example.dataService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName: Role
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/1 12:33
 * @Version: 1.0.0
 * @Description: TODO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private int id;
    private String name;
    private List<Permission> permissions;

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
