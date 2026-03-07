# 郑州市果蔬销售系统

## 项目概述

郑州市果蔬销售系统是一个面向本地果蔬销售的综合性电商平台，集成了在线交易、信息展示、社区互动、全流程溯源及AI智能问答等核心功能。系统采用前后端分离架构，为用户提供便捷、透明、可信赖的果蔬购买体验。

## 技术架构

### 整体架构
- **架构模式**：前后端分离（B/S架构）
- **前端框架**：Vue 3 + Vite + Element Plus
- **后端框架**：Spring Boot 2.7.18
- **数据库**：MySQL 8.0
- **接口文档**：Knife4j 3.0.8
- **认证方式**：JWT (JSON Web Token)

### 前端技术栈
- **Vue 3.5.25** - 渐进式JavaScript框架
- **Vite 7.3.1** - 下一代前端构建工具
- **Element Plus 2.13.2** - Vue 3 组件库
- **Pinia 3.0.4** - Vue状态管理
- **Vue Router 4.6.4** - 官方路由管理器
- **Axios 1.13.6** - HTTP客户端
- **ECharts 6.0.0** - 数据可视化库

### 后端技术栈
- **Spring Boot 2.7.18** - 核心框架
- **Spring Security** - 安全认证与权限控制
- **JWT 0.11.5** - Token认证机制
- **MyBatis Plus 3.5.3.1** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Knife4j 3.0.8** - 接口文档
- **Lombok 1.18.x** - 代码简化工具
- **Hutool 5.8.23** - 工具库

## 项目结构

```
zzguoshu/
├── fruit-veg-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fruitveg/
│   │   │   │   ├── common/              # 通用组件
│   │   │   │   ├── config/              # 配置类
│   │   │   │   ├── controller/          # 控制器层
│   │   │   │   │   ├── admin/           # 管理后台控制器
│   │   │   │   │   ├── ai/              # AI智能问答控制器
│   │   │   │   │   ├── auth/            # 认证控制器
│   │   │   │   │   ├── circle/          # 果蔬圈控制器
│   │   │   │   │   ├── merchant/        # 商家控制器
│   │   │   │   │   ├── order/           # 订单控制器
│   │   │   │   │   ├── product/         # 商品控制器
│   │   │   │   │   ├── trace/           # 溯源控制器
│   │   │   │   │   └── user/            # 用户控制器
│   │   │   │   ├── entity/              # 实体类
│   │   │   │   ├── mapper/              # Mapper接口
│   │   │   │   ├── service/             # 业务层
│   │   │   │   ├── security/            # 安全认证组件
│   │   │   │   └── utils/               # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml      # 配置文件
│   │   │       └── mapper/              # MyBatis映射文件
│   │   └── test/
│   └── pom.xml
├── fruit-veg-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── composables/        # 组合式函数
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面视图
│   │   │   ├── admin/          # 管理后台页面
│   │   │   ├── merchant/       # 商家页面
│   │   │   ├── public/         # 公共页面
│   │   │   └── user/           # 用户中心页面
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── sql/                        # 数据库脚本
│   └── init.sql
└── README.md                   # 本文件
```

## 功能模块

### 用户角色
- **系统管理员**：全局管理、审核监管、数据维护
- **商家**：店铺运营、商品管理、订单处理
- **用户**：浏览购物、互动交流、订单管理
- **游客**：有限浏览、引导注册

### 核心功能

#### 1. 用户模块
- 用户注册/登录（手机号验证码、账号密码）
- 个人信息管理
- 收货地址管理
- 账户安全设置

#### 2. 商家模块
- 商家入驻申请与审核
- 店铺信息管理
- 商品发布与管理
- 果蔬圈动态发布
- 溯源信息维护
- 订单处理与发货
- 销售数据统计

#### 3. 商品模块
- 商品分类管理（蔬菜、水果、菌菇等）
- 商品信息展示
- 商品搜索与筛选
- 商品详情查看
- 购物车管理

#### 4. 订单模块
- 在线下单
- 订单支付（模拟）
- 订单状态跟踪
- 订单查询与管理
- 售后服务

#### 5. 溯源模块
- 溯源信息录入
- 溯源信息查询
- 二维码扫码溯源
- 全流程信息追踪（产地、种植、采收、检测、物流）

#### 6. 社区模块
- 果蔬圈动态发布
- 动态浏览与互动
- 评论与点赞
- 商家与用户互动

#### 7. AI智能问答
- 果蔬知识科普
- 存储建议
- 食用方法咨询
- 智能问答服务

#### 8. 管理后台
- 用户管理
- 商家入驻审核
- 商品信息监管
- 溯源信息管理
- 订单与投诉处理
- 平台公告发布
- 数据统计分析

## 快速开始

### 环境要求
- **JDK**：1.8+
- **Node.js**：16+
- **Maven**：3.6+
- **MySQL**：8.0+
- **npm**：8+

### 数据库初始化

1. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS fruit_veg_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本
```bash
mysql -u root -p fruit_veg_sales < sql/init.sql
```

### 后端启动

1. 进入后端目录
```bash
cd fruit-veg-backend
```

2. 配置数据库连接
编辑 `src/main/resources/application.yml` 文件，修改数据库连接信息：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/fruit_veg_sales?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: 你的用户名
    password: 你的密码
```

3. 构建项目
```bash
mvn clean install
```

4. 启动后端服务
```bash
mvn spring-boot:run
```

后端服务启动成功后，访问：
- **应用地址**：http://localhost:8080
- **接口文档**：http://localhost:8080/doc.html

### 前端启动

1. 进入前端目录
```bash
cd fruit-veg-frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

前端服务启动成功后，访问：
- **应用地址**：http://localhost:5173

### 生产环境部署

#### 后端打包
```bash
cd fruit-veg-backend
mvn clean package
java -jar target/fruit-veg-backend-1.0.0-SNAPSHOT.jar
```

#### 前端打包
```bash
cd fruit-veg-frontend
npm run build
```

打包后的静态文件在 `dist` 目录，可部署到 Nginx 或其他静态服务器。

## 接口文档

项目使用 Knife4j 作为接口文档工具，提供完整的 API 文档和在线调试功能。

启动后端服务后访问：http://localhost:8080/doc.html

### 认证方式

系统使用 JWT (JSON Web Token) 进行认证。登录成功后，服务器会返回一个 Token，客户端需要在后续请求的 `Authorization` 头中携带该 Token：

```
Authorization: Bearer {token}
```

Token 有效期默认 2 小时。

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
- **biz_user_address** - 用户收货地址表

## 开发规范

### 代码规范
- 遵循阿里巴巴 Java 开发规范
- 遵循 Vue 3 风格指南
- 使用 ESLint 进行代码检查
- 所有 API 接口返回统一格式的响应

### 命名规范
- 包名：全部小写，使用域名倒写形式 + 项目名 + 模块名
- 类名：使用驼峰命名法，首字母大写
- 方法名：使用驼峰命名法，首字母小写
- 变量名：使用驼峰命名法，首字母小写

### Git 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
test: 测试相关
chore: 构建/工具链相关
```

## 项目特色

1. **全流程溯源**：从产地、种植、采收、检测到物流的完整信息追踪体系，增强消费者信任
2. **AI智能问答**：基于人工智能技术的智能问答模块，提供果蔬知识科普与咨询服务
3. **社区互动**：果蔬圈功能让商家分享种植过程与环境，增强与消费者的互动
4. **多角色权限**：完善的角色权限管理，支持系统管理员、商家、用户、游客四类角色
5. **响应式设计**：前端采用响应式设计，适配多种设备

## 版本历史

- **v1.0.0** (2026-02-27) - 项目初始化，完成核心功能开发

## 联系方式

**开发者**：罗明跃
**联系邮箱**：lniosy@example.com

## 许可证

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

## 相关文档

- [后端项目 README](./fruit-veg-backend/README.md)
- [前端项目 README](./fruit-veg-frontend/README.md)
- [产品需求文档 (PRD)](./郑州市果蔬销售系统PRD.md)
