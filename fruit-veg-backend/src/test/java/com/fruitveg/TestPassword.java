package com.fruitveg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TestPassword {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testPasswordMatch() {
        String encodedPassword = "$2a$10$gQcDiuivi6sJxJ3.6pbNJ.uRVawT0pogiT6TCSydPkUIJSNXNXL5y";
        String rawPassword = "123456";

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("Password matches: " + matches);

        assertTrue(matches, "Password should match");
    }
}
