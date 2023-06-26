package com.example.dataService.jpaRepoisitory;

import com.example.dataService.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Integer> {

}
