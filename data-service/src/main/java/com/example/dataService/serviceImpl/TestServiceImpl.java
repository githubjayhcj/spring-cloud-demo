package com.example.dataService.serviceImpl;

import com.example.dataService.common.DataResult;
import com.example.dataService.entity.Product;
import com.example.dataService.entity.User;
import com.example.dataService.mapper.UserMapper;
import com.example.dataService.openFeign.Data2ServiceClient;
import com.example.dataService.service.TestService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestServiceImpl implements TestService {

    @Value("${server.port}")
    private int serverPort;

    @Value("${spring.application.name}")
    private String serverName;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Data2ServiceClient data2ServiceClient;

    @Override
    @Transactional
    public DataResult saveService(List<User> users){
        for(int i =0;i<users.size();i++){
            System.out.println("all user =======");
            System.out.println(users.get(i).toString());
        }
        DataResult<List<User>> data = new DataResult<>();
        try {
            for (int i = 0;i<2;i++){
                int save1 = userMapper.insertUser(users.get(i));
                System.out.println("save"+i+"="+save1);
            }
            data.setCode(1);
            data.setMessage("save ok");
            data.setData(users);
        }catch (Exception e){
            String message = "serverName:"+serverName+". serverPort:"+serverPort+". class:TestServiceImpl. method:public DataResult saveService(List<User> users). IOException:"+e.getMessage();
            data.setCode(0);
            data.setMessage(message);

            // @Transactional 需要检测到抛出的异常才能生效，如果在事务内部捕获（处理掉了）异常。事务管理将失效。所以可以手动执行事务回滚。
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return data;
    }

    @Override
    @Transactional
    public int saveService2(List<User> users) throws IOException{

        System.out.println("all user =======");
        System.out.println(users.get(0).toString());
        System.out.println(users.get(1).toString());

        for (int i = 0;i<2;i++){
            int save1 = userMapper.insertUser(users.get(i));
            System.out.println("save"+i+"="+save1);

        }
        return 0;
    }

    @Override
    @GlobalTransactional
    public List<DataResult> transSave(User user, Product product) {
        System.err.println("开始全局事务，XID = " + RootContext.getXID());
        DataResult dataResult = data2ServiceClient.insertProduct(product);
        int save = userMapper.insertUser(user);

        DataResult dataResult1 = new DataResult(save);
        List<DataResult> datas = new ArrayList<>();
        datas.add(dataResult);
        datas.add(dataResult1);
        return datas;
    }
}
