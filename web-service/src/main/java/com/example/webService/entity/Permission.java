package com.example.webService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: Permission
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/1 12:35
 * @Version: 1.0.0
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private int id;
    private String name;

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
