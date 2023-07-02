package com.example.webService.serviceImpl;

import com.example.webService.common.DataResult;
import com.example.webService.entity.Product;
import com.example.webService.entity.User;
import com.example.webService.openFeign.Data2ServiceClient;
import com.example.webService.openFeign.DataServiceClient;
import com.example.webService.service.TransService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransServiceImpl implements TransService {

    @Autowired
    private DataServiceClient dataServiceClient;

    @Autowired
    private Data2ServiceClient data2ServiceClient;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<DataResult> transSave(User user, Product product) throws Exception{
        System.err.println("开始全局事务，XID = " + RootContext.getXID());

        DataResult dataResult = data2ServiceClient.insertProduct(product);
        DataResult dataResult2 = dataServiceClient.saveUser(user);
        List<DataResult> datas = new ArrayList<>();
        datas.add(dataResult);
        datas.add(dataResult2);
        if (dataResult2.getCode() == 0){
            throw new RuntimeException("trans user failed 测试抛异常后，分布式事务回滚！");
        }
        return datas;
    }
}
