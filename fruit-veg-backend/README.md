# 郑州市果蔬销售系统后端项目

## 项目概述

郑州市果蔬销售系统是一个面向本地果蔬销售的综合性电商平台，集成了在线交易、信息展示、社区互动、全流程溯源及AI智能问答等核心功能。本项目是系统的后端服务部分，使用Spring Boot 2.7框架开发。

## 技术架构

### 核心框架
- **Spring Boot 2.7.18** - 后端核心框架
- **Spring Security** - 安全认证与权限控制
- **JWT 0.11.5** - Token认证机制
- **MyBatis Plus 3.5.3.1** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Knife4j 3.0.3** - 接口文档
- **Lombok 1.18.x** - 代码简化工具
- **Hutool 5.8.23** - 工具库

### 项目结构
```
fruit-veg-backend
├── src/main/java/com/fruitveg
│   ├── common/              # 通用组件
│   │   └── Result.java      # 统一响应结果类
│   ├── config/              # 配置类
│   │   ├── Knife4jConfig.java       # 接口文档配置
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   └── JwtConfig.java           # JWT配置
│   ├── controller/          # 控制器层
│   │   ├── auth/            # 认证控制器
│   │   ├── user/            # 用户控制器
│   │   ├── merchant/        # 商家控制器
│   │   ├── product/         # 商品控制器
│   │   ├── order/           # 订单控制器
│   │   ├── trace/           # 溯源控制器
│   │   ├── circle/          # 果蔬圈控制器
│   │   └── admin/           # 管理后台控制器
│   ├── entity/              # 实体类
│   ├── mapper/              # Mapper接口
│   ├── service/             # 业务层
│   │   └── impl/            # 业务实现类
│   ├── security/            # 安全认证组件
│   └── utils/               # 工具类
└── sql/                     # 数据库脚本
    └── init.sql             # 数据库初始化脚本
```

## 数据库设计

### 核心表结构
- **sys_user** - 用户表
- **biz_merchant** - 商家表
- **biz_category** - 商品分类表
- **biz_product** - 商品表
- **biz_order** - 订单表
- **biz_order_item** - 订单明细表
- **biz_trace** - 溯源信息表
- **biz_circle** - 果蔬圈表

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+（可选）

### 安装与运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd fruit-veg-backend
   ```

2. **创建数据库**
   ```sql
   CREATE DATABASE IF NOT EXISTS fruit_veg_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **初始化数据**
   ```bash
   # 执行sql/init.sql脚本
   mysql -u root -p fruit_veg_sales < sql/init.sql
   ```

4. **配置数据库连接**
   编辑 `src/main/resources/application.yml` 文件，修改数据库连接信息：
   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/fruit_veg_sales?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
       username: 你的用户名
       password: 你的密码
   ```

5. **构建项目**
   ```bash
   mvn clean install
   ```

6. **启动项目**
   ```bash
   mvn spring-boot:run
   ```

### 访问项目

- **应用程序访问地址**：http://localhost:8080
- **接口文档访问地址**：http://localhost:8080/doc.html

## 接口文档

项目使用Knife4j作为接口文档工具，提供完整的API文档和在线调试功能。启动项目后访问：
http://localhost:8080/doc.html

## 认证方式

系统使用JWT (JSON Web Token) 进行认证。登录成功后，服务器会返回一个Token，客户端需要在后续请求的`Authorization`头中携带该Token：
```
Authorization: Bearer {token}
```

Token有效期默认2小时。

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发规范
- 使用Lombok简化代码
- 所有API接口返回统一格式的响应

### 命名规范
- 包名：全部小写，使用域名倒写形式 + 项目名 + 模块名
- 类名：使用驼峰命名法，首字母大写
- 方法名：使用驼峰命名法，首字母小写
- 变量名：使用驼峰命名法，首字母小写

## 功能模块

### 用户模块
- 用户注册/登录
- 用户信息管理
- 收货地址管理

### 商家模块
- 商家入驻申请
- 店铺信息管理
- 商品管理

### 商品模块
- 商品分类管理
- 商品信息管理
- 商品搜索与筛选

### 订单模块
- 购物车管理
- 订单创建与支付
- 订单查询与管理

### 溯源模块
- 溯源信息录入
- 溯源信息查询
- 物流信息跟踪

### 社区模块
- 果蔬圈动态发布
- 动态浏览与互动
- 评论与点赞

### 管理后台
- 用户管理
- 商家审核
- 商品监管
- 订单管理

## 版本历史

- v1.0.0 (2026-02-27) - 项目初始化

## 联系方式

**开发者**：罗明跃
**联系邮箱**：lniosy@example.com

## 许可证

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
