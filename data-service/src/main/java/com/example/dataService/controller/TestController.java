package com.example.dataService.controller;

import com.example.dataService.common.DataResult;
import com.example.dataService.entity.Product;
import com.example.dataService.entity.User;
import com.example.dataService.jpaRepoisitory.UserRepository;
import com.example.dataService.mapper.UserMapper;
import com.example.dataService.openFeign.Data2ServiceClient;
import com.example.dataService.service.TestService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RefreshScope
public class TestController {

    @Value("${myName.lastName:}")
    public String myName;

    @Value("${spring.application.name}")
    public String appName;

    @Value("${server.port}")
    public String port;



    // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestService testService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Data2ServiceClient data2ServiceClient;

    @RequestMapping("/user")
    public String user(){
        User user = new User();
        user.setId(7);
        user.setEmail("1178376362@11.com");
        user.setName("evinsaaa");
        userRepository.save(user);
        return user.toString();
    }

    @RequestMapping("/get/{id}")
    public String getById(@PathVariable int id){
        User user = userMapper.getById(id);
        return "data from "+appName+":"+port+". content:"+user.toString();
    }

    @RequestMapping("/selectById/{id}")
    public String selectById(@PathVariable int id){
        User user = userMapper.selectById(id);
        return user.toString();
    }

    @RequestMapping("/get")
    public String get() {
        System.err.println("myName="+myName);
        return "--------";
    }

    @RequestMapping("/testUser")
    public String testUser(){
//        TestUser testUser = new TestUser(7,"evins-testuser");
//        System.out.println("testUser="+testUser.toString());
        return "test data service 1";
    }

    @RequestMapping("/insert")
    public String insert() {
        User user = new User("evins-insert","123141231");
        int save = userMapper.insertUser(user);
        System.out.println("save="+save);
        return "insert=";
    }

    @PostMapping("/saveUser")
    public DataResult saveUser(@RequestBody User user) throws IOException {
        System.err.println("开始全局事务，XID = " + RootContext.getXID());
        System.out.println(user.toString());
        userMapper.insertUser(user);
        return new DataResult<User>(1,"save ok",user);
    }

    @RequestMapping("/saveAll")
    public DataResult saveAll() {
        List<User> users = new ArrayList<>();
        users.add(new User("evins-insert-eee","1178376362@11.com"));
        users.add(new User("evins-insert-fff","117837636@11.com117837636@11.com117837636@11.com"));
        DataResult data = new DataResult(users);
//        try {
//            data = testService.saveService(users);
////            testService.saveService2(users);
//            data.setMessage("save ok");
//        }catch (Exception e){
//            String message = "serverName:"+appName+". serverPort:"+port+". class:TestServiceImpl. method:public DataResult saveService(List<User> users). IOException:"+e.getMessage();
//            data.setCode(0);
//            data.setMessage(message);
//        }
        data = testService.saveService(users);
        return data;
    }

    @RequestMapping("/getUserById/{id}")
    public DataResult getUserById(@PathVariable int id){
        User user = userMapper.selectById(id);
        DataResult<User> data = new DataResult<>();
        data.setMessage("ok , service name:"+appName+". port:"+port);
        data.setData(user);
        return data;
    }

    @RequestMapping("/erro")
    public DataResult erro(){
        throw new RuntimeException("erro from : "+appName+":"+port);
    }

    @RequestMapping("/getStr")
    public DataResult getStr() {
        System.err.println("myName="+myName);
        return new DataResult("getStr");
    }
    @RequestMapping("/getPById/{id}")
    @GlobalTransactional
    public DataResult getStr(@PathVariable int id) {
        System.err.println("开始全局事务，XID = " + RootContext.getXID());

        return data2ServiceClient.getPById(id);
    }


    @RequestMapping("/trans")
    public List<DataResult> trans() {
        System.err.println("trans");
        User user = new User("tolong user","email aaaaemail email aaaaemailemail aaaaemailemail aaaaemail");
        Product product = new Product("tolong producr",1234);
        List<DataResult> dataResultList = testService.transSave(user,product);
        return dataResultList;
    }
}
