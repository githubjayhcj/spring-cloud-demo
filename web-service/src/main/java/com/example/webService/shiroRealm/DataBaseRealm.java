package com.example.webService.shiroRealm;

import com.example.webService.common.DataResult;
import com.example.webService.entity.User;
import com.example.webService.service.TestService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: DataBaseRealm
 * @Author: hong-chen-jie-(Evins) hongchenjie 
 * @Data: 2023/7/29 18:31 
 * @Version: 1.0.0
 * @Description: TODO
 */
@Component
public class DataBaseRealm extends AuthorizingRealm {

    @Autowired
    private TestService testService;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("in doGetAuthorizationInfo...");
        String username = (String) principalCollection.getPrimaryPrincipal();
        System.out.println("username:"+username);
        //授权对象
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole("manager");
        List<String> perms = new ArrayList<>();
        perms.add("add");
        perms.add("update");
        perms.add("delete");
        perms.add("select");
        simpleAuthorizationInfo.addStringPermissions(perms);

        return simpleAuthorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("in doGetAuthenticationInfo......");
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;

        String username = usernamePasswordToken.getUsername();
        String password = new String(usernamePasswordToken.getPassword());
        System.out.println("username:"+username+";password:"+password);


        return new SimpleAuthenticationInfo(username,password,getName());



//        DataResult<User> dataResult = testService.getUserByName(username);
//        System.out.println("dataResult:"+dataResult.toString());
//        if(dataResult.getCode() > 0){
//            User user = dataResult.getData();
//            if (user != null){
//                if(user.getPassword().equals(password)){
//                    System.out.println("return SimpleAuthenticationInfo...");
//                    return new SimpleAuthenticationInfo(username,password,getName());
//                }else {
//                    System.err.println("登录密码错误！");
//                    throw new AuthenticationException();
//                }
//            }else {
//                System.err.println("用户名不存在:"+username);
//                throw new UnknownAccountException();
//            }
//        }else {
//            System.err.println("查询 api 异常："+dataResult.getMessage());
//            throw new AuthenticationException();
//        }
    }
}
