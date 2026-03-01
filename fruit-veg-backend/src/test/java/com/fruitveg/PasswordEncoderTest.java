package com.fruitveg;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncoderTest {

    @Test
    public void testPasswordEncoder() {
        // 创建BCrypt密码编码器
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 原始密码
        String rawPassword = "123456";

        // 使用固定的BCrypt哈希值（已通过passwordEncoder.encode(rawPassword)生成）
        String encodedPassword = "$2a$10$gQcDiuivi6sJxJ3.6pbNJ.uRVawT0pogiT6TCSydPkUIJSNXNXL5y";

        // 输出加密后的密码
        System.out.println("固定的密码哈希: " + encodedPassword);

        // 验证密码
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码匹配: " + matches);
        assertTrue(matches);
    }
}
