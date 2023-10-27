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
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayOpenPublicTemplateMessageIndustryModifyRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayOpenPublicTemplateMessageIndustryModifyResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.webService.ESdao.MyElasticDao;
import com.example.webService.ESrepository.MyElasticRepository;
import com.example.webService.FlowRuleAndDegradeRule.DefaultRule;
import com.example.webService.blockHandleAndFallBack.DefaultExceptionUtil;
import com.example.webService.common.DataResult;
import com.example.webService.entity.*;
import com.example.webService.ioNetty.clientSocketUtil.ClientSocketUtil;
import com.example.webService.openFeign.Data2ServiceClient;
import com.example.webService.openFeign.DataServiceClient;
import com.example.webService.service.TestService;
import com.example.webService.service.TransService;
import com.example.webService.utils.JWTutils;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@CrossOrigin // 跨域
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

    @Autowired
    private JWTutils jwTutils;

    // client socket 工具类
    @Autowired
    private ClientSocketUtil clientSocketUtil;

    private RestTemplate restTemplate;

    //
    public TestController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @RequestMapping("/test")
    public String test(HttpServletRequest request) {

        HttpSession session = request.getSession();
        if (session.getAttribute("port") == null) {
            session.setAttribute("port", serverPort);
        } else {
            System.err.println("session port =" + session.getAttribute("port"));
        }
        return "--------serverPort=" + serverPort + "::session store port=" + session.getAttribute("port");
    }

    //微信登录
    @RequestMapping("/wxLogin")
    public String wxLogin(HttpServletRequest request, String code) {
        //  secretId : 0514e24936d39f5e746a6bff0878573f
        System.out.println("wxLogin: code ," + code);
        //
        //String res = this.restTemplate.getForObject("http://47.99.139.38:8084/getClientSocket",String.class);
        String res = this.restTemplate.getForObject("https://api.weixin.qq.com/sns/jscode2session?appid=wx54f55227e732f4df&secret=0514e24936d39f5e746a6bff0878573f&js_code=" + code + "&grant_type=authorization_code", String.class);
        System.out.println("res:" + res);
        JSONObject jsonObject = new JSONObject();
        jsonObject = JSONObject.parseObject(res);
        jsonObject.put("token", "this is token value");
        System.out.println("jsonObject:" + jsonObject);

        return jsonObject.toString();
    }
    //

    @RequestMapping("/setSession/{value}")
    public String set(HttpServletRequest request, @PathVariable String value) {
        HttpSession session = request.getSession();
        session.setAttribute("sessionVal", value);
        return "web service port:" + serverPort;
    }

    @RequestMapping("/getSession")
    public String get(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionValue = (String) session.getAttribute("sessionVal");
        return "web-service port:" + serverPort + ":sessionValue=" + sessionValue;
    }

    @GetMapping("/saveUser")
    public String saveUser() {
        User user = new User("saveUserName", "save user email");
        DataResult<User> data = dataServiceClient.saveUser(user);
        return "save user ok";
    }

    @RequestMapping("/getData/{id}")
    @SentinelResource("getData")
    public String getData(@PathVariable int id) {
        DataResult<User> data = dataServiceClient.getUserById(id);
//        System.err.println("getData data :"+data);
        return "current server is:" + appName + ". port:" + serverPort + ". config profile:" + serverProfile + ". " + data;
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
    public String exceptionHandler(long s, BlockException ex) {
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
    @SentinelResource(value = "getResourceC", blockHandler = "exceptionHandler", blockHandlerClass = {DefaultExceptionUtil.class}, fallback = "helloFallback", fallbackClass = {DefaultExceptionUtil.class})
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
    public String getResourceE() throws InterruptedException {
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
    @SentinelResource(value = "getResourceF", blockHandler = "exceptionHandlerF", blockHandlerClass = {DefaultExceptionUtil.class}, fallback = "helloFallbackF", fallbackClass = {DefaultExceptionUtil.class})
    public String getResourceF() throws InterruptedException {
        //调用初始化流控规则的方法
        DefaultRule.initFlowQpsRuleF();
        //调用初始化熔断规则的方法
        //DefaultRule.initDegradeRuleF();
        //Thread.sleep(30000);
        return "this is getResourceF";
    }


    @RequestMapping("/test2")
    public DataResult test2() {
        DataResult data = dataServiceClient.getUserById(1);
        System.out.println("test2:" + data.getMessage());
        DataResult data2 = dataServiceClient.erro();
        return data2;
    }

    //    @GlobalTransactional()
    @RequestMapping("/trans")
    public List<DataResult> trans() throws Exception {
        Product product = new Product("transProduct5555", 33333);
        User user = new User("transUser55555", "save user save usersave usersave usersave user");

        List<DataResult> datas = transService.transSave(user, product);
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

            Product product = new Product("transProduct5555", 33333);
            User user = new User("transUser55555", "save user save usersave usersave usersave usersave usersave user");

            dataResult = data2ServiceClient.insertProduct(product);
            dataResult2 = dataServiceClient.saveUser(user);
            datas.add(dataResult);
            datas.add(dataResult2);
            if (dataResult2.getCode() == 0) {
                System.err.println("开始手动事务回滚....");
                globalTransaction.rollback();
            }
        } catch (TransactionException transactionException) {
            dataResult.setCode(0);
            dataResult2.setCode(0);
            datas.add(dataResult);
            datas.add(dataResult2);
            return datas;
        }
        return datas;
    }


    @RequestMapping("/sendTopic/{msgStr}")
    public String sendTopic(@PathVariable String msgStr) throws Exception {
        Message<SimpleMsg> msg = new GenericMessage<SimpleMsg>(new SimpleMsg(msgStr));
        streamBridge.send("producer-out-0", msg);
        return "ok";
    }

    @RequestMapping("/redis/{key}/{value}")
    public String sendTopic(@PathVariable String key, @PathVariable String value) throws Exception {
        System.out.println("key:" + key + "value:" + value);
        this.stringRedisTemplate.opsForValue().set(key, value);
        System.out.println(stringRedisTemplate.opsForValue().get(key));
        return "redis value:" + stringRedisTemplate.opsForValue().get(key);

//        this.redisTemplate.opsForValue().set(key,value);
//        System.out.println(redisTemplate.opsForValue().get(key));
//        return "redis value:"+redisTemplate.opsForValue().get(key);
    }

    //    elasticsearchOperations  操作
    @RequestMapping("/es/{id}/{name}/{desc}")
    public String es(@PathVariable int id, @PathVariable String name, @PathVariable String desc) {
        System.out.println("elasticsearch :" + this.elasticsearchOperations);
        IndexOperations ido = this.elasticsearchOperations.indexOps(MyElastic.class);
        if (!ido.exists()) {
            ido.create();
        }
        MyElastic myElastic = new MyElastic();
        myElastic.setId(id);
        myElastic.setName(name);
        myElastic.setSubName("subname-" + name);
        myElastic.setContent("content-" + desc);
        myElastic.setDescription(desc);
        myElastic.setCreateDate(new Date());
        myElastic.setUpdateDate(new Date());
        this.elasticsearchOperations.save(myElastic);
        System.out.println("index :" + myElastic.toString());
        return "save es ok";
    }

    //
    @RequestMapping("/esto/{id}/{name}/{desc}")
    public String esto(@PathVariable int id, @PathVariable String name, @PathVariable String desc) {

        MyElastic myElastic = new MyElastic();
        myElastic.setId(id);
        myElastic.setName(name);
        myElastic.setSubName("subname-" + name);
        myElastic.setContent("content-" + desc);
        myElastic.setDescription(desc);
        myElastic.setCreateDate(new Date());
        myElastic.setUpdateDate(new Date());
        this.myElasticRepository.save(myElastic);
        System.out.println("index :" + myElastic.toString());
        return "save es ok";
    }

    @RequestMapping("/getes/{id}")
    public String getes(@PathVariable int id) {
        MyElastic myElastic = this.elasticsearchOperations.get(String.valueOf(id), MyElastic.class);
        System.out.println("myElastic name:" + myElastic.getName());
        return myElastic.toString() + "---port:" + serverPort;
    }

    @RequestMapping("/getName/{name}")
    public String getName(@PathVariable String name) {

        Pageable pageable = PageRequest.of(0, 5);

        // 分页查询
        Page<MyElastic> page = this.myElasticRepository.findByName(name, pageable);
        List<MyElastic> myElastics = page.getContent();
        for (MyElastic item : myElastics) {
            System.out.println("item::" + item.toString());
        }

        // 高亮(searchHit) 模糊查询
        SearchHits<MyElastic> searchHits = this.myElasticRepository.findByNameHit(name);
        List<SearchHit<MyElastic>> searchHitList = searchHits.getSearchHits();
        for (SearchHit<MyElastic> searchHit : searchHitList) {
            System.out.println("myElastic : " + searchHit.getContent().toString());
            System.out.println("score : " + searchHit.getScore());
            Map<String, List<String>> map = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                System.out.println("entry key:" + entry.getKey() + " . entry value:" + entry.getValue());
                List<String> values = entry.getValue();
                Iterator<String> iterator = values.iterator();
                while (iterator.hasNext()) {
                    System.out.println("value:" + iterator.next());
                }
            }
        }


        return "getName ok";
    }

    // 分页 + 高亮(searchHit) + 字段排序(id) 模糊查询
    @RequestMapping("/hitPage/{name}/{page}/{size}")
    public String hitPage(@PathVariable String name, @PathVariable int page, @PathVariable int size) {
        //分页对象  和原生DSL 语言分页存在差别  ： from , size
        Pageable pageable = PageRequest.of(page, size);

        // 排序
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        SearchHits<MyElastic> searchHits = myElasticDao.highlightPageSearch(name, pageable, sort);

        List<SearchHit<MyElastic>> searchHitsList = searchHits.getSearchHits();
        for (SearchHit<MyElastic> searchHit : searchHitsList) {
            System.out.println("--" + searchHit.getContent().toString());
            Map<String, List<String>> mapField = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> entry : mapField.entrySet()) {
                System.out.println("hit key:" + entry.getKey());
                System.out.println("hit value:" + entry.getValue());
            }
        }

        return "hitPage ok";
    }


    @RequestMapping("/login/{username}/{password}")
    public DataResult login(@PathVariable String username, @PathVariable String password) {
        System.out.println("username:" + username + " ; password:" + password);
        DataResult<User> dataResult = dataServiceClient.getUserByName(username);
        User user = dataResult.getData();
        System.out.println("user:" + user);
//        return dataServiceClient.getUserByName(username);
//        return testService.getUserByName(username);

        return dataResult;
    }


    // 以下为  shiro 登录 相关方法
    @PostMapping("/register")
    public DataResult<String> register(@RequestBody User user, HttpServletRequest request, HttpSession session) {
        System.out.println("user name:" + user.getName());
        System.out.println("user password:" + user.getPassword());

        System.out.println("session attri name:" + session.getAttribute("name"));
        if (session.getAttribute("name") == null) {
            session.setAttribute("name", user.getName());
        }

        // 生成随即盐
        user.setSalt(new SecureRandomNumberGenerator().nextBytes().toString());
        // 生成 hash code 加密字符码
        user.setPassword(new SimpleHash("md5", user.getPassword(), user.getSalt(), 2).toString());

//        DataResult<User> dataResult = dataServiceClient.saveUser(user);
        DataResult<User> dataResult = new DataResult<>();
        System.out.println("save dataResult" + dataResult);

        return new DataResult<String>("register ok", "ok");
    }


    @GetMapping("/admin/adminPage")
//    @RequiresRoles("manager")
    public DataResult<String> adminPage(HttpServletRequest request) {
        System.out.println("/admin/adminPage...");

        Subject subject = SecurityUtils.getSubject();

        System.out.println("isAuthentication :" + subject.isAuthenticated());
        System.out.println("hasRole :" + subject.hasRole("manager"));
        System.out.println("isPermitted :" + subject.isPermitted("add"));
        System.out.println("isRemembered :" + subject.isRemembered());

        return new DataResult<String>("OK", "adminPage , isLogin:" + subject.isAuthenticated() + "; port:" + serverPort);


    }

    @PostMapping("/login")
    public DataResult<String> login2(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("user name:" + user.getName());
        System.out.println("user password:" + user.getPassword());
//    @GetMapping ("/login/{name}/{password}")
//    public DataResult<String> login2(@PathVariable String name, @PathVariable String password,HttpServletRequest request) {
//        System.out.println("user name:"+name);
//        System.out.println("user password:"+password);
//        User user = new User();
//        user.setName(name);
//        user.setPassword(password);

        Subject subject = SecurityUtils.getSubject();

        try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(user.getName(), user.getPassword());
            usernamePasswordToken.setRememberMe(true);

            subject.login(usernamePasswordToken);

        } catch (UnknownAccountException e) {
            System.err.println("用户名异常:" + e);
            return new DataResult<>(0, "用户名异常", e.toString());
        } catch (AuthenticationException e) {
            System.err.println("用户登录验证异常:" + e);
            return new DataResult<>(0, "用户名异常", e.toString());
        }

        System.out.println("isAuthentication :" + subject.isAuthenticated());
        System.out.println("hasRole :" + subject.hasRole("manager"));
        System.out.println("isPermitted :" + subject.isPermitted("add"));
        System.out.println("isRemembered :" + subject.isRemembered());
        //String sessionId = (String) subject.getSession().getId();
        //System.out.println("sessionId :"+sessionId);
        //String session = Base64.getEncoder().encodeToString(sessionId.getBytes());
        //System.out.println("cookie SESSION:"+session);

        return new DataResult<>("login2 OK ; port:" + serverPort);
    }


    // JWT (json web token)
    @PostMapping("/jwtLogin")
    public DataResult jwtLogin(@RequestBody User user, HttpServletRequest request) {
        System.out.println("jwtLogin ....");
        Map<String, Object> payLoad = new HashMap<>();
        payLoad.put("name", user.getName());
        payLoad.put("password", user.getPassword());
        //
        String token = jwTutils.createJWT(payLoad);
        System.out.println("");

        return new DataResult("ok", token);
    }

    @PostMapping("/jwtPost")
    public DataResult jwtPost(@RequestBody User user, HttpServletRequest request) {
        System.out.println("jwtPost ....");
//        String token = request.getHeader("Authorization");
//        token = token.substring(new String("Bearer").length()+1);
//        System.out.println("token:"+token);
//        //
//        DecodedJWT decodedJWT = jwTutils.decodeAndVerifyToken(token);
//        System.out.println("decodedJWT:"+decodedJWT.getClaims());
        return new DataResult("ok");
    }

    /*
     * JWT 整合 shiro 思想:
     * 将 token 是否有效作为验证用户是否登录的凭据。
     * 登录成功签发token。（可选：将token通过subject.login 传入realm 进行直接登录(生成登录状态，会在redis中生成包含 subject 的 session 数据)）
     * 而后每次访问都提交 token ，并在 shiroFilter 中校验 token ，token 有效则执行 subject.login 生成登录状态（后续 controller 权限验证），无效则直接放行。
     * */
    @PostMapping("/jwtShiroLogin")
    public DataResult jwtShiroLogin(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("jwtShiroLogin user:" + user);

        DataResult<User> dataResult = testService.getUserByName(user.getName());
        System.out.println("dataResult:" + dataResult.toString());

        DataResult<Object> result = new DataResult();
        if (dataResult.getCode() > 0) {
            User dbUser = dataResult.getData();
            if (dbUser != null) {
                // 编码
                String encodePW = new SimpleHash("md5", user.getPassword(), dbUser.getSalt(), 2).toString();
                System.err.println("encodePW：" + encodePW);
                // 匹配
                if (dbUser.getPassword().equals(encodePW)) {
                    System.out.println("验证通过，生成token ，执行subject.login 登录验证，进入 SimpleAuthenticationInfo 直接登录...");
                    //
                    Map<String, Object> payLoad = new HashMap<>();
                    payLoad.put("name", user.getName());
                    payLoad.put("password", user.getPassword());
                    //
                    Subject subject = SecurityUtils.getSubject();
                    //String sessionId = (String) subject.getSession().getId();
                    //String session = Base64.getEncoder().encodeToString(sessionId.getBytes());

                    String token = jwTutils.createJWT(payLoad);
                    System.out.println("token:" + token);
                    // 可选（登录时可无需创建登录状态）
                    UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(token, token);

                    subject.login(usernamePasswordToken);
                    //
                    System.err.println("登录成功！");
                    System.out.println("isAuthenticationx :" + subject.isAuthenticated());
                    System.out.println("hasRolex :" + subject.hasRole("manager"));
                    System.out.println("isPermittedx :" + subject.isPermitted("add"));
                    System.out.println("isRememberedx :" + subject.isRemembered());
                    //result.setMessage(session);
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("user", user);
                    result.setData(data);

                    //Cookie cookie = new Cookie("mysession",session);
                    // Domain 和 Path 标识定义了 Cookie 的作用域：即允许 Cookie 应该发送给哪些 URL。
                    //cookie.setDomain("http://localhost:8084");
                    //cookie.setPath("/");
                    //response.addCookie(cookie);
                } else {
                    System.err.println("登录密码错误！");
                    result.setCode(0);
                    result.setMessage("登录密码错误！");
                    throw new AuthenticationException();
                }
            } else {
                result.setCode(0);
                result.setMessage("用户名不存在！");
            }
        } else {
            result.setCode(0);
            result.setMessage("系统查询异常！");
        }
        return result;
    }

    @PostMapping("/admin/jwtShiroAdminData")
    public DataResult jwtShiroAdminData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("jwtShiroAdminData ...");
        //System.out.println("token:"+request.getHeader("Authorization"));
        // 前后端分离，代码形式做权限验证、及验证异常提示
        //Subject subject = SecurityUtils.getSubject();
        Subject subject = (Subject) request.getAttribute("subject");
        String token = (String) request.getAttribute("token");
        subject.login(new UsernamePasswordToken(token, token));
        if (!subject.isPermittedAll("add")) {
            System.out.println("没有 add 权限，无权访问：/admin/jwtShiroAdminData");
            request.setAttribute("dataResult", new DataResult<String>("没有 add 权限，无权访问：/admin/jwtShiroAdminData", "no authentication"));
            request.getRequestDispatcher("/systemFeedBack").forward(request, response);
        }

        System.out.println("isAuthentication :" + subject.isAuthenticated());
        System.out.println("hasRole :" + subject.hasRole("manager"));
        System.out.println("isPermitted :" + subject.isPermitted("add"));

        return new DataResult("jwtShiroAdminData ok");
    }

    // 需要登录 返回信息处理（前后端分离提示信息）
    @RequestMapping("/systemFeedBack")
    public DataResult systemFeedBack(HttpServletRequest request, DataResult dataResult) {

        System.out.println("systemFeedBack...");
        dataResult = (DataResult) request.getAttribute("dataResult");
        System.out.println("dataResult:" + dataResult);
        return dataResult;
    }
    // shiro权限错误 /error
//    @RequestMapping("/error")
//    public DataResult systemError(HttpServletRequest request, DataResult dataResult) {
//
//        System.out.println("systemError...");
//        dataResult = (DataResult) request.getAttribute("dataResult");
//        System.out.println("dataResult:" + dataResult);
//        return dataResult;
//    }

    // 后端管理 - token / 权限 - 相关接口
    @PostMapping("/adminWeb/login")
    public DataResult adminWebLogin(HttpServletRequest request, @RequestBody User user, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("adminWebLogin ....");
        System.out.println("user:" + user);

        DataResult<User> dataResult = testService.getUserByName(user.getName());
        System.out.println("dataResult:" + dataResult.toString());

        DataResult<Object> result = new DataResult();
        if (dataResult.getCode() > 0) {
            User dbUser = dataResult.getData();
            if (dbUser != null) {
                // 编码
                String encodePW = new SimpleHash("md5", user.getPassword(), dbUser.getSalt(), 2).toString();
                System.out.println("encodePW：" + encodePW);
                // 匹配
                if (dbUser.getPassword().equals(encodePW)) {
                    System.out.println("验证通过，生成token ，执行subject.login 登录验证，进入 SimpleAuthenticationInfo 直接登录...");
                    //
                    Map<String, Object> payLoad = new HashMap<>();
                    payLoad.put("id", dbUser.getId());
                    payLoad.put("name", user.getName());
                    payLoad.put("password", user.getPassword());
                    //
                    Subject subject = SecurityUtils.getSubject();
                    //String sessionId = (String) subject.getSession().getId();
                    //String session = Base64.getEncoder().encodeToString(sessionId.getBytes());

                    String token = jwTutils.createJWT(payLoad);
                    System.out.println("token:" + token);
                    // 可选（登录时可无需创建登录状态）
                    UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(token, token);

                    subject.login(usernamePasswordToken);
                    //
                    System.err.println("登录成功！");
                    System.out.println("isAuthenticationx :" + subject.isAuthenticated());
                    System.out.println("hasRolex :" + subject.hasRole("manager"));
                    System.out.println("isPermittedx :" + subject.isPermitted("add"));
                    System.out.println("isRememberedx :" + subject.isRemembered());
                    //result.setMessage(session);
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("user", user);
                    result.setData(data);
                } else {
                    System.out.println("登录密码错误！");
                    result.setCode(0);
                    result.setMessage("登录密码错误！");
                    //throw new AuthenticationException();
                }
            } else {
                System.out.println("用户不存在");
                result.setCode(0);
                result.setMessage("用户名不存在！");
            }
        } else {
            result.setCode(0);
            result.setMessage("系统查询异常！");
        }
        return result;
    }

    //
    @PostMapping("/adminWeb/getUserInfo")
    public DataResult getUserInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("getUserInfo in ...");
        DecodedJWT decodedJWT = jwTutils.decodeAndVerifyToken((String) request.getAttribute("token"));
        System.out.println("decodedJWT" + decodedJWT.getClaims());
        System.out.println("user id:" + Integer.valueOf(decodedJWT.getClaim("id").toString().split("\"")[1]));
        User user = new User();
        user.setName(decodedJWT.getClaim("name").toString().split("\"")[1].toString());
        user.setId(Integer.valueOf(decodedJWT.getClaim("id").toString().split("\"")[1]));
        System.out.println("user:" + user);
        // roles / permissions
        List<Role> roles = testService.getRolesByUid(user.getId()).getData();
        System.out.println("roles :" + roles);
        List<Permission> permissions = testService.getPermsByUid(user.getId()).getData();
        System.out.println("permissions :" + permissions);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", roles);
        data.put("permissions", permissions);
        System.out.println("data:" + data);
        return new DataResult("getUserInfo ok", data);
    }

    // springDoc 接口文档示例
    @GetMapping("/springDoc")
    //@Hidden
    public DataResult springDoc(@Parameter(name = "自定义参数名称", description = "参数描述...") String name) {
        System.out.println("springDoc test...");
        return new DataResult("springDoc test");
    }

    // springDoc 接口文档示例
    @GetMapping("/sent/{name}/{msg}")
    //@Hidden
    public DataResult sentMsg(@PathVariable String name, @PathVariable String msg) {
        System.out.println("sentMsg  name : " + name + " msg:" + msg);
        //
        DataResult dataResult = this.clientSocketUtil.sentMsgByName(name, msg);
        return dataResult;
    }

    // socket（netty 相关）
    @GetMapping("/payment/{channelId}")
    //@Hidden
    public DataResult payment(@PathVariable String channelId) {
        System.out.println("payment  channelId : " + channelId);
        // 生成待处理事件
        PendingEvent event = new PendingEvent();
        event.setId((int) (Math.random() * 1000));
        event.setSocketChannelId(Integer.parseInt(channelId));
        event.setStatus(6);
        this.clientSocketUtil.addEvent(event);
        // 发送处理
        DataResult dataResult = this.clientSocketUtil.sentMsgByName(channelId, event.getId() + "," + channelId + "," + 6 + ",check payment 已支付");
        return dataResult;
    }

    @GetMapping("/checkEvent/{channelId}")
    //@Hidden
    public DataResult checkEvent(@PathVariable int eventId) {
        System.out.println("checkEvent  eventId : " + eventId);
        //
        int count = this.clientSocketUtil.checkEvent(eventId);

        return new DataResult<>("check count:" + count);
    }

    // alipay sdk api
    @RequestMapping("/alipay/{out_trade_no}")
    public DataResult alipay(HttpServletResponse httpServletResponse,@PathVariable String out_trade_no) {
        System.out.println("alipay..."+out_trade_no);
        DataResult<String> dataResult = new DataResult<>();

        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        //设置应用ID
        alipayConfig.setAppId("9021000130638460");
        //设置应用私钥
        alipayConfig.setPrivateKey("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDKtdeN0SKnnFlhC9WdgeBjKD2+VUbOan7krK4DEkQmwtOXWVLY8n/fvkuybFGUMu283R2ebwdHHWgJcGSUbhzRqtTxrQBVk+IdXvjUMT878C/PO0IQ9V3qdP5YOQEq4w4nF/Iz2GLFOwAb6qavqENnCiGFHpNW+s7MPzzeS4W9QTzSBtcG8AT1i8MjNHxjdlfn8Nj9MnaqVgZusglYcCq+oY6vZHgSqt/4BDG15mGCZxaCOJujyEWVTNRwTlNbukSxg4+CQcyw+AwOnC1K+OvCKEi7wvVfITHgDCyXFbxB9Bff7dW9NXljWAa0pT6cEl2112ezfVi7hZlb5OoG7iNPAgMBAAECggEAPjbFK19udWbE/8X0D91Wktg86vedPajtvR1KYuLuS78r1Z2ykacLnA35iY85EnT3BioO9xs4bpkV3x/BntSUv7zgkjP4kEtM8Jq/X9Clia3T3pRfWHkWE+lITqdgNlttco4k5ciZt9K929OOyQapeSxg0UxrCBz97y3RbqBbMZQSUz3Pr6Zw4mUNQbQL/Gbu3nq/E+rrORUAam82ZB28+00Y1rygGUdGvTQ/I4CzLxITXreqfaQdumheSZ0xT5/rLpeJR+DHGMDgBXuWZNnGLrflVXmOpL6efjVkj5+xChQ/3RZg8/KVUO4ATWY9Pyl/YQWrc2mJZYZBsLORlt48oQKBgQDk3N4jV5jos3JUxwNRhbtexx6TLyPR38F6e+Td5oYTtfIoxB9Iu/gbQC9T2D07+TgTWbfhhapohsFu1vKlnAIsUPpTyzXrQBEsWtsSiI/NSpACifPuEpu95d3dQLh8qlwXBjclgy4jHiHjXVtm+BV5jF+sZKL8L06h/8lYgXTz+QKBgQDivx3SIsbTPi4a28FmHpPoviM5k/6eOVTqcblugHKQsfWTVStjcjKTyaMsoGSP9zwQqigt8kAIMjsndr798mjeiXve/YE9pQirCApdhaXKVRo4iIWxAMBdinx43eiN/waulrAR0Q77my5vW2E6KJ+4K1aITt2pDTl/DDbTstYThwKBgFRbBJXKR9YdrlqJMDPwMuvwQNjHW1CUROA8olMYEPta6PMVtuI8Rmrk8YzZ+1gLuBuJyjkZ54G3ibytBar9Id/ryRjxoeIgLzRGGNFv/HEStpn49ywdN8J645gVrupkcKUb6V+uRmczSBJh6rq9Xkz67JjCT0DcvwL/INu6e70hAoGBAMl+SAO6P7DsapAXhI840PtcK3U5HGSCUgFcOgoGPshyO+ZHIx1mF6fY4RYBGhwM+eVdMANUaK5f5M026pZEWgiXZ+pKoBSlmURTCtL8ts1MPtbKCvO11TvKmr3VhJjabZer9J6Ahq3zwpnWPFowS26fpG/QlY1qYROTSQ1quG31AoGBAJmB1l+6VCIlvUowQcg+WQwSoQsxkWX8hssxQQH8ERgQhrlsahg9W/fSSZtiZQlfuqnU3TJ2CVNdW2ug2IzFnBdam6x2BFA9VAxlFGB3XGcHlzLzZlDChqw2xt2DZR0iH28Fbmj6uX4dI7IPg9AD5fL//Ix0XPFoQURTFYWhpF2x");
        //设置请求格式，固定值json
        alipayConfig.setFormat("JSON");
        //设置字符集
        alipayConfig.setCharset(String.valueOf(StandardCharsets.UTF_8));
        //设置签名类型
        alipayConfig.setSignType("RSA2");
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjsUk3VoNRRRSqDbIAyv6ZZkBx92Bn03rwZBFn9+CrTUGhjRJLloewD9a6g4/TT7C6SCxDP+VydnXJ4ynLG4Udr8eTP7+CobBbi1rEZfm2lqt1HhW79sWiJf6raKVKzWzQuN9hDOjsbW2P5PUxXQunGOWGJ5Gk7EaEqitHp0NmvSHEB0FLeyG3JP7+05sBPH0qiW0Gz1Shbn9Evuxy5jz9srNof2FOvXffp2zgqZ3YljeFgya8ultqWxIbNoRf8x7W2kCt5MKjPeDrgZ8eXUlkngpyvlI/OBcYH8HUKaOZNejgHH72IGj1sBCLzekxjuSrxn2dpx3qw2NdGdpXR4KXQIDAQAB");
        try {
            //实例化客户端
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            //请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            //异步接收地址，仅支持http/https，公网可访问
            request.setNotifyUrl("https://5d91-101-71-38-69.ngrok-free.app/alipayCallBack");
            //同步跳转地址，仅支持http/https
            request.setReturnUrl("https://747e-101-71-38-69.ngrok-free.app/");
            /******必传参数******/
            JSONObject bizContent = new JSONObject();
            //商户订单号，商家自定义，保持唯一性
            bizContent.put("out_trade_no", out_trade_no);
            //支付金额，最小值0.01元
            bizContent.put("total_amount", 0.01);
            //订单标题，不可使用特殊符号
            bizContent.put("subject", "测试商品");
            //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

            /******可选参数******/
            //bizContent.put("time_expire", "2022-08-01 22:00:00");

            //// 商品明细信息，按需传入
            //JSONArray goodsDetail = new JSONArray();
            //JSONObject goods1 = new JSONObject();
            //goods1.put("goods_id", "goodsNo1");
            //goods1.put("goods_name", "子商品1");
            //goods1.put("quantity", 1);
            //goods1.put("price", 0.01);
            //goodsDetail.add(goods1);
            //bizContent.put("goods_detail", goodsDetail);

            //// 扩展信息，按需传入
            //JSONObject extendParams = new JSONObject();
            //extendParams.put("sys_service_provider_id", "2088511833207846");
            //bizContent.put("extend_params", extendParams);

            request.setBizContent(bizContent.toString());
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if(response.isSuccess()){
                System.out.println("调用成功");
                System.out.println("form:"+response.getBody());
                //
//                httpServletResponse.setContentType("text/html;charset="+StandardCharsets.UTF_8);
//                httpServletResponse.getWriter().write(response.getBody());
//                httpServletResponse.getWriter().flush();
//                httpServletResponse.getWriter().close();
                dataResult.setData(response.getBody());
            } else {
                System.out.println("调用失败");
            }
        }catch (AlipayApiException e){
            System.out.println("AlipayApiException....");
        }
        return dataResult;
    }

    @RequestMapping("/alipayCallBack")
    public void alipayCallBack(HttpServletRequest request) {
        System.out.println("alipayCallBack....支付完成回调成功");
        System.out.println(request.getParameterMap().keySet().toString());

    }
}
