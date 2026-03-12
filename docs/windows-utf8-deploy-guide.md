# Windows 部署中文防乱码指南（MySQL + Spring Boot + Vue）

本文用于解决在 Windows 部署时中文显示为 `???` 的问题，适用于本项目。

## 1. 已落地的代码级修复

1. 后端请求/响应强制 UTF-8（`server.servlet.encoding.*`）。
2. Tomcat URI 编码设置为 UTF-8（`server.tomcat.uri-encoding`）。
3. MySQL JDBC 增强编码参数（`characterEncoding=UTF-8`、`connectionCollation=utf8mb4_unicode_ci`）。
4. Hikari 初始化执行 `SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci`。
5. Maven 编译与资源处理统一 UTF-8（`project.build.sourceEncoding` + compiler/resources plugin）。

## 2. Windows 导库时必须执行的步骤

在 Windows `cmd` 中，默认代码页常导致 SQL 导入时中文被写坏。请按以下顺序执行：

```bat
cd /d D:\your\project\path
scripts\windows\import_utf8.cmd sql\init.sql fruit_veg_sales
```

脚本会自动做三件事：

1. 切换代码页 `chcp 65001`。
2. 使用 `mysql --default-character-set=utf8mb4` 导入 SQL。
3. 自动运行 `sql/windows/check_charset.sql` 输出字符集检查结果。

## 3. 一次性修复数据库字符集（可选）

如果历史库不是 `utf8mb4`，先备份后执行：

```sql
SOURCE sql/windows/fix_charset.sql;
SOURCE sql/windows/check_charset.sql;
```

或在命令行中：

```bat
mysql --default-character-set=utf8mb4 -u root -p fruit_veg_sales < sql\windows\fix_charset.sql
mysql --default-character-set=utf8mb4 -u root -p fruit_veg_sales < sql\windows\check_charset.sql
```

## 4. 关键结论

1. 若数据已经被写成 `???`，字符集修复无法“反解”原文，必须重新导入正确源数据。
2. 只有“代码 UTF-8 + 数据库 utf8mb4 + 导库命令 utf8mb4”三者同时满足，才能彻底避免复发。
3. 新环境建议先跑 `check_charset.sql`，确认无误后再启动业务服务。

## 5. Windows 启动后端建议

建议显式指定 JVM 编码：

```bat
java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar fruit-veg-backend.jar
```

用于避免在某些 Windows 终端环境下因默认编码导致的日志或文本处理异常。
