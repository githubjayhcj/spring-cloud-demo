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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: DataBaseRealm
 * @Author: hong-chen-jie-(Evins) hongchenjie 
 * @Data: 2023/7/29 18:31 
 * @Version: 1.0.0
 * @Description: shiro整合JWT - json web token
 * 修改登录验证逻辑，直接生成 token 进行 subject.login 登录
 */
@Component
public class DataBaseRealmByJWT extends AuthorizingRealm {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TestService testService;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("in DataBaseRealmByJWT doGetAuthorizationInfo...");
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
        System.out.println("in DataBaseRealmByJWT doGetAuthenticationInfo......");

        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;

        String tokenByusername = usernamePasswordToken.getUsername();
        String tokenBypassword = new String(usernamePasswordToken.getPassword());
        System.out.println("token byname:"+tokenByusername+";token bypassword:"+tokenBypassword);

        // 只要传入SimpleAuthenticationInfo 中的值相同，两次重复登录(subject.login()) , 并不会在redis中生成两条相同的session（包含subject对象）值
        return new SimpleAuthenticationInfo(tokenByusername,tokenBypassword,getName());
    }
}
