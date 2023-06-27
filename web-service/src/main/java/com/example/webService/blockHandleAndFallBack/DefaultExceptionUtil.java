package com.example.webService.blockHandleAndFallBack;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public class DefaultExceptionUtil {

    // 不同类中的异常调用方法 必须为 public static 函数.

    // Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
    public static String exceptionHandler(BlockException ex) {
        // Do some log here.
        System.err.println("resourceA blockHandler");
        return "getResourceC 限流";
    }

    // Fallback 函数，函数签名与原函数一致或加一个 Throwable 类型的参数.
    public static String helloFallback() {
        System.err.println("resourceA blockHandler");
        return "getResourceC 熔断 ";
    }

    // Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
    public static String exceptionHandlerF(BlockException ex) {
        // Do some log here.
        System.err.println("getResourceF blockHandler");
        return "getResourceF 限流";
    }

    // Fallback 函数，函数签名与原函数一致或加一个 Throwable 类型的参数.
    public static String helloFallbackF() {
        System.err.println("getResourceF blockHandler");
        return "getResourceF 熔断 ";
    }

}
