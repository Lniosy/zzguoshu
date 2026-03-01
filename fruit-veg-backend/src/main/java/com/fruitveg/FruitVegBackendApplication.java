package com.fruitveg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 郑州市果蔬销售系统后端启动类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@SpringBootApplication
@MapperScan("com.fruitveg.mapper")
public class FruitVegBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitVegBackendApplication.class, args);
        System.out.println("================================================");
        System.out.println("           果蔬销售系统后端服务启动成功！          ");
        System.out.println("------------------------------------------------");
        System.out.println("         访问地址: http://localhost:8080          ");
        System.out.println("         接口文档: http://localhost:8080/doc.html ");
        System.out.println("================================================\n");
    }

}
