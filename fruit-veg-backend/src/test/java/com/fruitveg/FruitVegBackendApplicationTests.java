package com.fruitveg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用程序启动测试
 *
 * @author lniosy
 * @since 2026-02-27
 */
@SpringBootTest
class FruitVegBackendApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("项目启动成功！");
    }

    @Test
    void printProjectInfo() {
        System.out.println("================================================");
        System.out.println("           郑州市果蔬销售系统后端项目            ");
        System.out.println("------------------------------------------------");
        System.out.println("项目名称：fruit-veg-backend");
        System.out.println("项目版本：1.0.0-SNAPSHOT");
        System.out.println("框架版本：Spring Boot 2.7.18");
        System.out.println("构建工具：Maven 3.x");
        System.out.println("数据库：MySQL 8.0+");
        System.out.println("ORM框架：MyBatis Plus 3.5.3.1");
        System.out.println("认证方式：JWT + Spring Security");
        System.out.println("接口文档：Knife4j 3.0.3");
        System.out.println("工具库：Hutool 5.8.23");
        System.out.println("================================================");
    }
}
