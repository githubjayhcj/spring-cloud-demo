package com.example.webService.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName: JWTutils
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/8/10 02:32
 * @Version: 1.0.0
 * @Description: JWT json-web-token 工具类
 */
@Component
public class JWTutils {
    // 密钥
    private static final String SECURE_KEY = "hong-chen-jie";
    private static int SECONDS =  24*60*60;

    // 生成密钥法
    private Algorithm createAlgorithm(){
        return Algorithm.HMAC256(SECURE_KEY);
    }

    // 创建token
    public String createJWT(Map<String,Object> customPayload){
        // 密钥算法
        Algorithm algorithm = createAlgorithm();
        JWTCreator.Builder builder = JWT.create();
        // 有效期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,11);
        System.out.println("time:"+calendar.getTime());
        // 设置有效期限
        builder.withExpiresAt(calendar.getTime());
        // 装载自定义参数
        for (Map.Entry<String,Object> entry : customPayload.entrySet()){
            builder.withClaim(entry.getKey(),entry.getValue().toString());
        }
        // 生成令牌
        String token = builder.sign(algorithm);
        System.out.println("token:"+token);
        return token;
    }

    // 解析和验证token
    public DecodedJWT decodeAndVerifyToken(String token){
        Algorithm algorithm = createAlgorithm();
        DecodedJWT decodedJWT = null;
        try {
            decodedJWT = JWT.require(algorithm).build().verify(token);
        }catch (JWTVerificationException e){
            System.err.println("JWTVerificationException:"+e.getStackTrace());
            throw new JWTVerificationException("decodeAndVerifyToken exception.");
        }
        return decodedJWT;
    }
}
