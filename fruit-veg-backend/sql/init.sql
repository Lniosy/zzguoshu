-- 果蔬销售系统数据库初始化脚本
-- 创建时间：2026-02-28
-- 数据库版本：MySQL 8.0.33

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fruit_veg_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fruit_veg_sales;

-- ==============================================
-- 系统用户表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    phone VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    birthday DATE COMMENT '生日',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ==============================================
-- 商家信息表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
    shop_logo VARCHAR(255) COMMENT '店铺Logo',
    shop_desc VARCHAR(500) COMMENT '店铺简介',
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    business_license VARCHAR(255) COMMENT '营业执照URL',
    food_license VARCHAR(255) COMMENT '食品经营许可证URL',
    address VARCHAR(500) COMMENT '经营地址',
    status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1正常 2禁用',
    audit_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家信息表';

-- ==============================================
-- 商品分类表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) COMMENT '分类图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ==============================================
-- 商品信息表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(255) COMMENT '主图URL',
    images VARCHAR(2000) COMMENT '详情图片JSON',
    price DECIMAL(10,2) NOT NULL COMMENT '销售价格',
    original_price DECIMAL(10,2) COMMENT '原价',
    stock INT DEFAULT 0 COMMENT '库存数量',
    unit VARCHAR(20) COMMENT '单位',
    description TEXT COMMENT '商品详情',
    sales INT DEFAULT 0 COMMENT '销量',
    status TINYINT DEFAULT 2 COMMENT '状态：0下架 1上架 2审核中',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- ==============================================
-- 订单表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    status TINYINT DEFAULT 0 COMMENT '订单状态',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    receiver_address VARCHAR(500) COMMENT '收货地址',
    logistics_company VARCHAR(100) COMMENT '物流公司',
    logistics_no VARCHAR(100) COMMENT '物流单号',
    ship_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ==============================================
-- 订单明细表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(255) COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ==============================================
-- 溯源信息表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_trace (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    origin_name VARCHAR(100) COMMENT '产地名称',
    origin_address VARCHAR(500) COMMENT '产地地址',
    origin_longitude DECIMAL(10,6) COMMENT '经度',
    origin_latitude DECIMAL(10,6) COMMENT '纬度',
    plant_method VARCHAR(200) COMMENT '种植方式',
    plant_time DATETIME COMMENT '种植时间',
    harvest_time DATETIME COMMENT '采收时间',
    fertilizer_record TEXT COMMENT '施肥记录JSON',
    pesticide_record TEXT COMMENT '农药记录JSON',
    test_report VARCHAR(255) COMMENT '检测报告URL',
    storage_condition VARCHAR(200) COMMENT '储存条件',
    shelf_life INT COMMENT '保质期（天）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源信息表';

-- ==============================================
-- 收货地址表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收货地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址：0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ==============================================
-- 果蔬圈表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_circle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    title VARCHAR(200) NOT NULL COMMENT '动态标题',
    content TEXT COMMENT '动态内容',
    images VARCHAR(2000) COMMENT '图片URLs JSON',
    product_ids VARCHAR(200) COMMENT '关联商品IDs',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    status TINYINT DEFAULT 1 COMMENT '状态：0草稿 1发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='果蔬圈表';

-- ==============================================
-- 初始化数据
-- ==============================================

-- 插入默认管理员用户（密码：123456，已加密）
INSERT INTO sys_user (username, password, phone, nickname, avatar, gender, status, create_time)
VALUES ('admin', '$2a$10$7JB720yubVSdLb.CiXH2ehema5dH3tqqK50tZxW9j8nP1uF12K3qQ', '13800138000', '管理员', '', 0, 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 插入默认分类
INSERT INTO biz_category (parent_id, name, icon, sort, status, create_time)
VALUES
(0, '新鲜水果', '/images/category/fruit.png', 1, 1, NOW()),
(0, '新鲜蔬菜', '/images/category/vegetable.png', 2, 1, NOW()),
(0, '有机食品', '/images/category/organic.png', 3, 1, NOW())
ON DUPLICATE KEY UPDATE name=name;

-- 插入测试商家
INSERT INTO biz_merchant (user_id, shop_name, shop_logo, shop_desc, contact_name, contact_phone, business_license, food_license, address, status, audit_time, create_time)
VALUES
(1, '绿源果蔬店', '/images/shop/green.png', '专注于新鲜有机果蔬的销售', '张三', '13900139000', '/images/license/1.png', '/images/license/2.png', '北京市朝阳区建国路88号', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE shop_name=shop_name;

-- 插入测试商品
INSERT INTO biz_product (merchant_id, category_id, name, main_image, images, price, original_price, stock, unit, description, sales, status, sort, create_time)
VALUES
(1, 1, '进口车厘子', '/images/product/cherry.jpg', '["/images/product/cherry1.jpg","/images/product/cherry2.jpg"]', 99.00, 128.00, 500, '盒', '新鲜进口车厘子，果肉饱满，甜度高', 100, 1, 1, NOW()),
(1, 2, '有机西红柿', '/images/product/tomato.jpg', '["/images/product/tomato1.jpg"]', 15.00, 20.00, 1000, '斤', '有机种植西红柿，无农药残留', 50, 1, 2, NOW()),
(1, 3, '有机黄瓜', '/images/product/cucumber.jpg', '["/images/product/cucumber1.jpg"]', 8.00, 12.00, 800, '斤', '有机种植黄瓜，清脆爽口', 30, 1, 3, NOW())
ON DUPLICATE KEY UPDATE name=name;

-- ==============================================
-- 结束初始化
-- ==============================================
