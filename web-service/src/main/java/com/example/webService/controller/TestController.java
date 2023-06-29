package com.example.webService.controller;


import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.example.webService.FlowRuleAndDegradeRule.DefaultRule;
import com.example.webService.blockHandleAndFallBack.DefaultExceptionUtil;
import com.example.webService.common.DataResult;
import com.example.webService.entity.Product;
import com.example.webService.entity.User;
import com.example.webService.openFeign.Data2ServiceClient;
import com.example.webService.openFeign.DataServiceClient;
import com.example.webService.service.TestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    @Value("${server.port}")
    private String serverPort;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${serverProfile:}")
    private String serverProfile;

    @Autowired
    private DataServiceClient dataServiceClient;

    @Autowired
    private Data2ServiceClient data2ServiceClient;


    @Autowired
    private TestService testService;

    @RequestMapping("/test")
    public String test(HttpServletRequest request) {

        HttpSession session = request.getSession();
        if (session.getAttribute("port") == null){
            session.setAttribute("port",serverPort);
        }else {
            System.err.println("session port ="+session.getAttribute("port"));
        }
        return "--------serverPort="+serverPort+"::session store port="+session.getAttribute("port");
    }

    @RequestMapping("/setSession/{value}")
    public String set(HttpServletRequest request, @PathVariable String value) {
        HttpSession session = request.getSession();
        session.setAttribute("sessionVal",value);
        return serverPort+"ok";
    }

    @RequestMapping("/getSession")
    public String get(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionValue = (String) session.getAttribute("sessionVal");
        return serverPort+":sessionValue="+sessionValue;
    }

    @GetMapping("/saveUser")
    public String saveUser(){
        User user = new User("saveUserName","save user email");
        DataResult<User> data = dataServiceClient.saveUser(user);
        return "save user ok";
    }

    @RequestMapping("/getData/{id}")
    @SentinelResource("getData")
    public String getData(@PathVariable int id){
        DataResult<User> data = dataServiceClient.getUserById(id);
//        System.err.println("getData data :"+data);
        return "current server is:"+appName+". port:"+serverPort+". config profile:"+serverProfile+". "+data;
    }


    //  以下 为 sentinel 可视化管理端 定义 流控\熔断。代码执行回调
    @RequestMapping(value = "/getResourceA/{s}")
    @SentinelResource(value = "getResourceA/{s}", blockHandler = "exceptionHandler", fallback = "helloFallback")
    public String getResourceA(@PathVariable long s) {

//        throw new RuntimeException("getResourceA/{s} failed");

//        return testService.resourceA();
        return "this is getResourceA";
    }

    // Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
    public String exceptionHandler(long s,BlockException ex) {
        // Do some log here.
        System.err.println("getResourceA/{s} blockHandler");
        return "getResourceA/{s} 限流";
    }

    // Fallback 函数，函数签名与原函数一致或加一个 Throwable 类型的参数.
    public String helloFallback(long s) {
        System.err.println("getResourceA/{s} fallback");
        return "getResourceA/{s} 熔断 ";
    }

    @RequestMapping(value = "/getResourceB")
    public String getResourceB() {

        return testService.resourceA();
    }

    @RequestMapping(value = "/getResourceC")
    @SentinelResource(value = "getResourceC", blockHandler = "exceptionHandler", blockHandlerClass = {DefaultExceptionUtil.class}, fallback = "helloFallback",fallbackClass = {DefaultExceptionUtil.class})
    public String getResourceC() {
//        throw new RuntimeException("getResourceC failed");
        return "this is getResourceC";
    }


    @RequestMapping(value = "/getResourceD")
    public String getResourceD() {
        Entry entry = null;
        // 务必保证finally会被执行
        try {
            // 资源名可使用任意有业务语义的字符串
            entry = SphU.entry("getResourceD");
            // 被保护的业务逻辑
            // do something...
//            throw new RuntimeException("getResourceD failed");
            return "this is getResourceD";
        } catch (Exception e1) {//BlockException
            // 资源访问阻止，被限流或被降级
            // 进行相应的处理操作
            System.err.println("getResourceD blockhandle/fallback");
            return "getResourceD blockhandle/fallback";
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    // 以下为 代码方式定义流控\熔断规则

    @RequestMapping(value = "/getResourceE")
    public String getResourceE() throws InterruptedException{
        // 初始化流控规则
        //initFlowQpsRuleE();
        // 初始化熔断规则
        initDegradeRuleE();
        // 资源名可使用任意有业务语义的字符串
        if (SphO.entry("getResourceE")) {
            // 务必保证finally会被执行
            try {
                /**
                 * 被保护的业务逻辑
                 */
                //throw new RuntimeException("getResourceE failed");
                Thread.sleep(30000);
                return "this is getResourceE";
            } finally {
                SphO.exit();
            }
        } else {
            // 资源访问阻止，被限流或被降级
            // 进行相应的处理操作
            System.err.println("getResourceE blockhandle/fallback");
            return "getResourceE blockhandle/fallback";
        }
    }
    //    流控规则
    private static void initFlowQpsRuleE() {
        List<FlowRule> rules = new ArrayList<>();
        // 定义一个限流规则对象
        FlowRule rule1 = new FlowRule();
        // 资源名称
        rule1.setResource("getResourceE");
        // Set max qps to 2  每秒访问量
        rule1.setCount(2);
        // 限流阈值的类型
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }
    //   熔断规则
    private static void initDegradeRuleE() {
        List<DegradeRule> rules = new ArrayList<>();
        // 资源名，即规则的作用对象
        DegradeRule rule = new DegradeRule("getResourceE");
        // 熔断策略，支持慢调用比例/异常比例/异常数策略
        rule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        // 慢调用比例模式下为慢调用临界 RT（超出该值计为慢调用）；异常比例/异常数模式下为对应的阈值
        rule.setCount(3); // Threshold is 70% error ratio
        // 熔断触发的最小请求数，请求数小于该值时即使异常比率超出阈值也不会熔断（1.7.0 引入）
        rule.setMinRequestAmount(3);
        // 统计时长（单位为 ms），如 60*1000 代表分钟级（1.8.0 引入）
        rule.setStatIntervalMs(3000); // 3s
        // 熔断时长，单位为 s
        rule.setTimeWindow(5);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }


    @RequestMapping(value = "/getResourceF")
    @SentinelResource(value = "getResourceF", blockHandler = "exceptionHandlerF", blockHandlerClass = {DefaultExceptionUtil.class}, fallback = "helloFallbackF",fallbackClass = {DefaultExceptionUtil.class})
    public String getResourceF() throws InterruptedException {
        //调用初始化流控规则的方法
        DefaultRule.initFlowQpsRuleF();
        //调用初始化熔断规则的方法
        //DefaultRule.initDegradeRuleF();
        //Thread.sleep(30000);
        return "this is getResourceF";
    }


    @RequestMapping("/test2")
    public DataResult test2(){
        DataResult data = dataServiceClient.getUserById(1);
        System.out.println("test2:"+data.getMessage());
        DataResult data2 = dataServiceClient.erro();
        return data2;
    }

    @RequestMapping("/trans")
    public DataResult trans(){
        DataResult data = data2ServiceClient.insertProduct(new Product("webServiceProduct",33333));
        System.out.println("test:"+data.getMessage());
        return data;
    }



}
