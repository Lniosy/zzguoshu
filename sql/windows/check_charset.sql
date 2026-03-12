-- 在目标库执行:
-- mysql --default-character-set=utf8mb4 -u root -p fruit_veg_sales < check_charset.sql

SET NAMES utf8mb4;

SELECT 'SCHEMA_CHARSET' AS section, SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = DATABASE();

SELECT 'TABLE_COLLATION' AS section, TABLE_NAME, TABLE_COLLATION
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

SELECT 'COLUMN_CHARSET' AS section, TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_SET_NAME, COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND DATA_TYPE IN ('char', 'varchar', 'text', 'tinytext', 'mediumtext', 'longtext')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- 快速排查可疑乱码（仅用于提示，不保证覆盖所有情况）
SELECT 'SUSPECT_PRODUCT_NAME' AS section, id, name
FROM biz_product
WHERE name LIKE '%?%';

SELECT 'SUSPECT_NOTICE_TITLE' AS section, id, title
FROM biz_notice
WHERE title LIKE '%?%';
