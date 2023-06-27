package com.example.webService.serviceImpl;




import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.webService.service.TestService;
import org.springframework.stereotype.Service;


@Service
public class TestServiceImpl implements TestService {


    @SentinelResource(value = "resourceA",blockHandler = "exceptionHandler", fallback = "helloFallback")
    @Override
    public String resourceA() {
//        throw new RuntimeException("resourceA failed");
        return "this is resourceA";
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
