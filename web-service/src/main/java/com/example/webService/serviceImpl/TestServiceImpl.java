package com.example.webService.serviceImpl;




import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.webService.common.DataResult;
import com.example.webService.entity.Permission;
import com.example.webService.entity.Role;
import com.example.webService.entity.User;
import com.example.webService.openFeign.DataServiceClient;
import com.example.webService.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private DataServiceClient dataServiceClient;

    @SentinelResource(value = "resourceA",blockHandler = "exceptionHandler", fallback = "helloFallback")
    @Override
    public String resourceA() {
//        throw new RuntimeException("resourceA failed");
        return "this is resourceA";
    }

    @Override
    public DataResult<User> getUserByName(String name) {
        return this.dataServiceClient.getUserByName(name);
    }

    @Override
    public DataResult<List<Role>> getRolesByUid(int id) {
        return dataServiceClient.getRolesByUid(id);
    }

    @Override
    public DataResult<List<Permission>> getPermsByUid(int id) {
        return dataServiceClient.getPermsByUid(id);
    }

    // Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
    public String exceptionHandler(BlockException ex) {
        // Do some log here.
        System.err.println("resourceA blockHandler");
        return "resourceA 限流";
    }

    // Fallback 函数，函数签名与原函数一致或加一个 Throwable 类型的参数.
    public String helloFallback() {
        System.err.println("resourceA blockHandler");
        return "resourceA 熔断 ";
    }
}
