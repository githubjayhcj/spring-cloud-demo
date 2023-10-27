package com.example.webService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName: ApplicationTest
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/10/21 12:17
 * @Version: 1.0.0
 * @Description: TODO
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebServiceApplication.class)
public class ApplicationTest {

    @Before
    public void before(){
        System.out.println("before...");
    }

    @After
    public void after(){
        System.out.println("after...");


    }

    @Test
    public void test(){
        System.out.println("test...");
    }

}
