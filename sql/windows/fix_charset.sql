-- 用途: 将当前库与所有表统一为 utf8mb4 / utf8mb4_unicode_ci
-- 注意: 该脚本无法恢复已经写成 "???" 的历史数据。
-- 执行前建议先备份。

SET NAMES utf8mb4;
SET SESSION group_concat_max_len = 1024 * 1024;

SET @db := DATABASE();

SET @sql_db := CONCAT(
  'ALTER DATABASE `', @db, '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
);
PREPARE stmt_db FROM @sql_db;
EXECUTE stmt_db;
DEALLOCATE PREPARE stmt_db;

SELECT GROUP_CONCAT(
         CONCAT(
           'ALTER TABLE `', TABLE_NAME,
           '` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
         )
         SEPARATOR ';'
       )
INTO @sql_tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @db
  AND TABLE_TYPE = 'BASE TABLE';

SET @sql_tables := CONCAT(@sql_tables, ';');
PREPARE stmt_tables FROM @sql_tables;
EXECUTE stmt_tables;
DEALLOCATE PREPARE stmt_tables;

-- 执行后建议运行 check_charset.sql 复核
