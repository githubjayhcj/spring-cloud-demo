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
import com.example.webService.ESdao.MyElasticDao;
import com.example.webService.ESrepository.MyElasticRepository;
import com.example.webService.FlowRuleAndDegradeRule.DefaultRule;
import com.example.webService.blockHandleAndFallBack.DefaultExceptionUtil;
import com.example.webService.common.DataResult;
import com.example.webService.entity.MyElastic;
import com.example.webService.entity.Product;
import com.example.webService.entity.SimpleMsg;
import com.example.webService.entity.User;
import com.example.webService.openFeign.Data2ServiceClient;
import com.example.webService.openFeign.DataServiceClient;
import com.example.webService.service.TestService;
import com.example.webService.service.TransService;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Order;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

    @Autowired
    private TransService transService;

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private MyElasticRepository myElasticRepository;

    @Autowired
    private MyElasticDao myElasticDao;


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
        return "web service port:"+serverPort;
    }

    @RequestMapping("/getSession")
    public String get(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionValue = (String) session.getAttribute("sessionVal");
        return "web-service port:"+serverPort+":sessionValue="+sessionValue;
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

//    @GlobalTransactional()
    @RequestMapping("/trans")
    public List<DataResult> trans() throws Exception{
        Product product = new Product("transProduct5555",33333);
        User user = new User("transUser55555","save user save usersave usersave usersave user");

        List<DataResult> datas = transService.transSave(user,product);
        return datas;
    }

    // 手动执行全局事务操作
    @RequestMapping("/trans2")
    public List<DataResult> trans2() {
        DataResult dataResult = null;
        DataResult dataResult2 = null;
        List<DataResult> datas = new ArrayList<>();
        try {
            GlobalTransaction globalTransaction = GlobalTransactionContext.getCurrentOrCreate();
            globalTransaction.begin();
            System.err.println("开始全局事务，XID = " + RootContext.getXID());

            Product product = new Product("transProduct5555",33333);
            User user = new User("transUser55555","save user save usersave usersave usersave usersave usersave user");

            dataResult = data2ServiceClient.insertProduct(product);
            dataResult2 = dataServiceClient.saveUser(user);
            datas.add(dataResult);
            datas.add(dataResult2);
            if (dataResult2.getCode() == 0){
                System.err.println("开始手动事务回滚....");
                globalTransaction.rollback();
            }
        }catch (TransactionException transactionException){
            dataResult.setCode(0);
            dataResult2.setCode(0);
            datas.add(dataResult);
            datas.add(dataResult2);
            return datas;
        }
        return datas;
    }


    @RequestMapping("/sendTopic/{msgStr}")
    public String sendTopic(@PathVariable String msgStr) throws Exception{
        Message<SimpleMsg> msg = new GenericMessage<SimpleMsg>(new SimpleMsg(msgStr));
        streamBridge.send("producer-out-0", msg);
        return "ok";
    }

    @RequestMapping("/redis/{key}/{value}")
    public String sendTopic(@PathVariable String key,@PathVariable String value) throws Exception{
        System.out.println("key:"+key+"value:"+value);
        this.stringRedisTemplate.opsForValue().set(key,value);
        System.out.println(stringRedisTemplate.opsForValue().get(key));
        return "redis value:"+stringRedisTemplate.opsForValue().get(key);

//        this.redisTemplate.opsForValue().set(key,value);
//        System.out.println(redisTemplate.opsForValue().get(key));
//        return "redis value:"+redisTemplate.opsForValue().get(key);
    }

    //    elasticsearchOperations  操作
    @RequestMapping("/es/{id}/{name}/{desc}")
    public String es(@PathVariable int id, @PathVariable String name,@PathVariable String desc){
        System.out.println("elasticsearch :"+this.elasticsearchOperations);
        IndexOperations ido = this.elasticsearchOperations.indexOps(MyElastic.class);
        if (!ido.exists()){
            ido.create();
        }
        MyElastic myElastic = new MyElastic();
        myElastic.setId(id);
        myElastic.setName(name);
        myElastic.setSubName("subname-"+name);
        myElastic.setContent("content-"+desc);
        myElastic.setDescription(desc);
        myElastic.setCreateDate(new Date());
        myElastic.setUpdateDate(new Date());
        this.elasticsearchOperations.save(myElastic);
        System.out.println("index :"+myElastic.toString());
        return "save es ok";
    }

    //
    @RequestMapping("/esto/{id}/{name}/{desc}")
    public String esto(@PathVariable int id, @PathVariable String name,@PathVariable String desc){

        MyElastic myElastic = new MyElastic();
        myElastic.setId(id);
        myElastic.setName(name);
        myElastic.setSubName("subname-"+name);
        myElastic.setContent("content-"+desc);
        myElastic.setDescription(desc);
        myElastic.setCreateDate(new Date());
        myElastic.setUpdateDate(new Date());
        this.myElasticRepository.save(myElastic);
        System.out.println("index :"+myElastic.toString());
        return "save es ok";
    }

    @RequestMapping("/getes/{id}")
    public String getes(@PathVariable int id){
        MyElastic myElastic = this.elasticsearchOperations.get(String.valueOf(id),MyElastic.class);
        System.out.println("myElastic name:"+myElastic.getName());
        return myElastic.toString();
    }
    @RequestMapping("/getName/{name}")
    public String getName(@PathVariable String name){

        Pageable pageable = PageRequest.of(0,5);

        // 分页查询
        Page<MyElastic> page = this.myElasticRepository.findByName(name,pageable);
        List<MyElastic> myElastics = page.getContent();
        for (MyElastic item : myElastics){
            System.out.println("item::"+item.toString());
        }

        // 高亮(searchHit) 模糊查询
        SearchHits<MyElastic> searchHits = this.myElasticRepository.findByNameHit(name);
        List<SearchHit<MyElastic>> searchHitList = searchHits.getSearchHits();
        for(SearchHit<MyElastic> searchHit : searchHitList){
            System.out.println("myElastic : "+ searchHit.getContent().toString());
            System.out.println("score : "+ searchHit.getScore());
            Map<String, List<String>> map = searchHit.getHighlightFields();
            for (Map.Entry<String,List<String>> entry : map.entrySet()){
                System.out.println("entry key:"+entry.getKey()+" . entry value:"+entry.getValue());
                List<String> values = entry.getValue();
                Iterator<String> iterator = values.iterator();
                while (iterator.hasNext()){
                    System.out.println("value:"+iterator.next());
                }
            }
        }



        return "getName ok";
    }

    // 分页 + 高亮(searchHit) + 字段排序(id) 模糊查询
    @RequestMapping("/hitPage/{name}/{page}/{size}")
    public String hitPage(@PathVariable String name, @PathVariable int page, @PathVariable int size){
        //分页对象  和原生DSL 语言分页存在差别  ： from , size
        Pageable pageable = PageRequest.of(page,size);

        // 排序
        Sort sort = Sort.by(Sort.Direction.DESC,"id");

        SearchHits<MyElastic> searchHits = myElasticDao.highlightPageSearch(name,pageable,sort);

        List<SearchHit<MyElastic>> searchHitsList =  searchHits.getSearchHits();
        for(SearchHit<MyElastic> searchHit : searchHitsList){
            System.out.println("--"+searchHit.getContent().toString());
            Map<String,List<String>> mapField =  searchHit.getHighlightFields();
            for (Map.Entry<String,List<String>> entry : mapField.entrySet()){
                System.out.println("hit key:"+entry.getKey());
                System.out.println("hit value:"+entry.getValue());
            }
        }

        return "hitPage ok";
    }



}
