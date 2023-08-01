package com.example.webService.shiroRealm;

import com.alibaba.fastjson.JSON;
import com.example.webService.common.DataResult;
import com.example.webService.entity.Permission;
import com.example.webService.entity.Role;
import com.example.webService.entity.User;
import com.example.webService.service.TestService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

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
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TestService testService;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("in doGetAuthorizationInfo...");
        String username = (String) principalCollection.getPrimaryPrincipal();
        System.out.println("username:"+username);
        //授权对象
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        // 获取 roles 、 permission  从redis 中获取 user
        User user = JSON.parseObject(stringRedisTemplate.opsForValue().get("user"),User.class);
        //System.out.println("user xxx:"+user);
        List<Role> roles = testService.getRolesByUid(user.getId()).getData();
        //System.out.println("roles :"+roles);
        Set<String> roleSet = new HashSet<>();
        for (Role role : roles){
            roleSet.add(role.getName());
        }
        //System.out.println("roleSet:"+roleSet);
        simpleAuthorizationInfo.setRoles(roleSet);
        //
        List<Permission> permissions = testService.getPermsByUid(user.getId()).getData();
        Iterator<Permission> iterator = permissions.iterator();
        Set<String> permSet = new HashSet<>();
        while (iterator.hasNext()){
            Permission perm = iterator.next();
            permSet.add(perm.getName());
        }
        //System.out.println("permSet:"+permSet);
        simpleAuthorizationInfo.setStringPermissions(permSet);

        //simpleAuthorizationInfo.addRole("manager");
        //simpleAuthorizationInfo.addStringPermission("add");

        return simpleAuthorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("in doGetAuthenticationInfo......");
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;

        String username = usernamePasswordToken.getUsername();
        String password = new String(usernamePasswordToken.getPassword());
        System.out.println("username:"+username+";password:"+password);

        DataResult<User> dataResult = testService.getUserByName(username);
        System.out.println("dataResult:"+dataResult.toString());
        if(dataResult.getCode() > 0){
            User user = dataResult.getData();
            if (user != null){
                // 编码
                String encodePW = new SimpleHash("md5",password,user.getSalt(),2).toString();
                // 匹配
                if(user.getPassword().equals(encodePW)){
                    System.out.println("return SimpleAuthenticationInfo...");
                    // 用户信息缓存入 redis
                    this.stringRedisTemplate.opsForValue().set("user", JSON.toJSONString(user));
                    return new SimpleAuthenticationInfo(username,password,getName());
                }else {
                    System.err.println("登录密码错误！");
                    throw new AuthenticationException();
                }
            }else {
                System.err.println("用户名不存在:"+username);
                throw new UnknownAccountException();
            }
        }else {
            System.err.println("查询 api 异常："+dataResult.getMessage());
            throw new AuthenticationException();
        }
    }
}
