@echo off
setlocal
chcp 65001 >nul

REM 用法:
REM   import_utf8.cmd <sql文件路径> [数据库名]
REM 示例:
REM   import_utf8.cmd D:\deploy\init.sql fruit_veg_sales

if "%~1"=="" (
  echo [ERROR] 缺少 SQL 文件路径
  echo 用法: %~nx0 ^<sql文件路径^> [数据库名]
  exit /b 1
)

set "SQL_FILE=%~1"
if not exist "%SQL_FILE%" (
  echo [ERROR] SQL 文件不存在: %SQL_FILE%
  exit /b 1
)

set "DB_HOST=127.0.0.1"
set "DB_PORT=3306"
set "DB_USER=root"
set "DB_PASS=root"
set "DB_NAME=fruit_veg_sales"

if not "%~2"=="" set "DB_NAME=%~2"

set "SCRIPT_DIR=%~dp0"
set "CHECK_SQL=%SCRIPT_DIR%..\..\sql\windows\check_charset.sql"

echo [1/3] 正在按 utf8mb4 导入 SQL...
mysql --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_FILE%"
if errorlevel 1 (
  echo [ERROR] 导入失败，请检查 MySQL 服务、账号密码、库名和 SQL 文件编码。
  exit /b 1
)

echo [2/3] 正在执行字符集校验 SQL...
if exist "%CHECK_SQL%" (
  mysql --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%CHECK_SQL%"
) else (
  echo [WARN] 未找到校验脚本: %CHECK_SQL%
)

echo [3/3] 输出服务端字符集变量...
mysql --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "SHOW VARIABLES LIKE 'character_set_server'; SHOW VARIABLES LIKE 'collation_server';"

echo [OK] 导入完成。
endlocal
