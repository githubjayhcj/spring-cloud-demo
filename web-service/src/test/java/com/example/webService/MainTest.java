package com.example.webService;

import cn.hutool.crypto.KeyUtil;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.util.Base64;

/**
 * @ClassName: MainTest
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/10/15 22:41
 * @Version: 1.0.0
 * @Description: TODO
 */
public class MainTest {

    public static void main(String[] args) {
//       AntPathMatcher requestPath = new AntPathMatcher();
//       //
//       String pattern = "/admin/**";
//       //
//       String requestUrl = "/admin/daw/dwad";
//       boolean matcherPath = requestPath.match(pattern,requestUrl);
//        System.out.println("pattern url="+matcherPath);


        //
        KeyPair keyPair = KeyUtil.generateKeyPair("RSA",1024);
        byte[] key =  keyPair.getPublic().getEncoded();
        //String publicckey = new String(key, Charset.defaultCharset());
        String publicckey = Base64.getEncoder().encodeToString(key);
        System.out.println("==="+publicckey);
    }
}

