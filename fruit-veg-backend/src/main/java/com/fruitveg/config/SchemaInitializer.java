package com.fruitveg.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biz_user_address ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "user_id BIGINT NOT NULL,"
                        + "receiver_name VARCHAR(50) NOT NULL,"
                        + "receiver_phone VARCHAR(20) NOT NULL,"
                        + "receiver_address VARCHAR(500) NOT NULL,"
                        + "is_default TINYINT DEFAULT 0,"
                        + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                        + "deleted TINYINT DEFAULT 0,"
                        + "INDEX idx_user_id (user_id),"
                        + "INDEX idx_is_default (is_default)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }
}
