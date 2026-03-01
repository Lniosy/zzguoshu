package com.fruitveg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 快速测试类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@SpringBootTest
class QuickTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        System.out.println("✅ 应用程序上下文加载成功！");
    }

    @Test
    void printLoadedBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("✅ 应用程序加载了 " + beanNames.length + " 个Bean");

        System.out.println("\n=== 核心组件 ===");
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            String className = bean.getClass().getName();

            if (className.contains("Controller") || className.contains("Service") ||
                className.contains("Mapper") || className.contains("Config") ||
                className.contains("Security") || className.contains("Jwt")) {
                System.out.println("- " + beanName);
            }
        }
    }
}
