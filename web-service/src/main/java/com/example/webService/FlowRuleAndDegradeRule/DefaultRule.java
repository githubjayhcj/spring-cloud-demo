package com.example.webService.FlowRuleAndDegradeRule;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.List;

public class DefaultRule {

    //    流控规则
    public static void initFlowQpsRuleF() {
        List<FlowRule> rules = new ArrayList<>();
        // 定义一个限流规则对象
        FlowRule rule1 = new FlowRule();
        // 资源名称
        rule1.setResource("getResourceF");
        // Set max qps to 2  每秒访问量
        rule1.setCount(2);
        // 限流阈值的类型
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }
    //   熔断规则
    public static void initDegradeRuleF() {
        List<DegradeRule> rules = new ArrayList<>();
        // 资源名，即规则的作用对象
        DegradeRule rule = new DegradeRule("getResourceF");
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
}
