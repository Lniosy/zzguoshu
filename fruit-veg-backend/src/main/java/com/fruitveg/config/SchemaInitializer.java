package com.fruitveg.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// @Component
@Order(1)
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
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biz_banner ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "title VARCHAR(200) NOT NULL,"
                        + "image_url VARCHAR(500) NOT NULL,"
                        + "target_url VARCHAR(500) NULL,"
                        + "sort INT DEFAULT 0,"
                        + "status TINYINT DEFAULT 1,"
                        + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                        + "INDEX idx_status (status)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biz_notice ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "type VARCHAR(50) NOT NULL,"
                        + "title VARCHAR(200) NOT NULL,"
                        + "content VARCHAR(1000) NULL,"
                        + "publish_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "status TINYINT DEFAULT 1,"
                        + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                        + "INDEX idx_status (status),"
                        + "INDEX idx_publish_time (publish_time)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biz_runtime_state ("
                        + "state_key VARCHAR(64) PRIMARY KEY,"
                        + "state_json LONGTEXT NOT NULL,"
                        + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );

        // Backward-compatible migration:
        // old init scripts may create business tables without logic-delete column.
        ensureSoftDeleteColumns();
    }

    private void ensureSoftDeleteColumns() {
        ensureColumnExists("sys_user", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_merchant", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_category", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_product", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_order", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_order_item", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_trace", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_user_address", "deleted", "TINYINT DEFAULT 0");
        ensureColumnExists("biz_circle", "deleted", "TINYINT DEFAULT 0");
    }

    private void ensureColumnExists(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }
}
