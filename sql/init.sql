-- --------------------------------------------------
-- 郑州市果蔬销售系统数据库初始化脚本
-- 数据库名: fruit_veg_sales
-- 创建时间: 2026-02-27
-- --------------------------------------------------

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fruit_veg_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fruit_veg_sales;

-- --------------------------------------------------
-- 1. 用户表 (sys_user)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    nickname VARCHAR(50) NULL COMMENT '昵称',
    avatar VARCHAR(255) NULL COMMENT '头像URL',
    gender TINYINT(1) NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    birthday DATE NULL COMMENT '生日',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------------
-- 2. 商家表 (biz_merchant)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_merchant (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
    shop_logo VARCHAR(255) NULL COMMENT '店铺Logo',
    shop_desc VARCHAR(500) NULL COMMENT '店铺简介',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(11) NOT NULL COMMENT '联系电话',
    business_license VARCHAR(255) NOT NULL COMMENT '营业执照URL',
    food_license VARCHAR(255) NULL COMMENT '食品经营许可证URL',
    address VARCHAR(255) NOT NULL COMMENT '经营地址',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1正常 2禁用',
    audit_time DATETIME NULL COMMENT '审核时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- --------------------------------------------------
-- 3. 商品分类表 (biz_category)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT NULL DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) NULL COMMENT '分类图标',
    sort INT(11) NULL DEFAULT 0 COMMENT '排序',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- --------------------------------------------------
-- 4. 商品表 (biz_product)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(255) NOT NULL COMMENT '主图URL',
    images TEXT NULL COMMENT '详情图片JSON',
    price DECIMAL(10,2) NOT NULL COMMENT '销售价格',
    original_price DECIMAL(10,2) NULL COMMENT '原价',
    stock INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    description TEXT NULL COMMENT '商品详情',
    sales INT(11) NOT NULL DEFAULT 0 COMMENT '销量',
    status TINYINT(1) NOT NULL DEFAULT 2 COMMENT '状态：0下架 1上架 2审核中',
    sort INT(11) NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- --------------------------------------------------
-- 5. 订单表 (biz_order)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT(1) NULL COMMENT '支付方式',
    pay_time DATETIME NULL COMMENT '支付时间',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '订单状态',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(11) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(255) NOT NULL COMMENT '收货地址',
    logistics_company VARCHAR(50) NULL COMMENT '物流公司',
    logistics_no VARCHAR(50) NULL COMMENT '物流单号',
    ship_time DATETIME NULL COMMENT '发货时间',
    receive_time DATETIME NULL COMMENT '收货时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- --------------------------------------------------
-- 6. 订单明细表 (biz_order_item)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(255) NULL COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT(11) NOT NULL DEFAULT 1 COMMENT '数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- --------------------------------------------------
-- 7. 溯源信息表 (biz_trace)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_trace (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    origin_name VARCHAR(100) NOT NULL COMMENT '产地名称',
    origin_address VARCHAR(255) NOT NULL COMMENT '产地地址',
    origin_longitude DECIMAL(10,6) NULL COMMENT '经度',
    origin_latitude DECIMAL(10,6) NULL COMMENT '纬度',
    plant_method VARCHAR(50) NOT NULL COMMENT '种植方式',
    plant_time DATE NOT NULL COMMENT '种植时间',
    harvest_time DATE NOT NULL COMMENT '采收时间',
    fertilizer_record TEXT NULL COMMENT '施肥记录JSON',
    pesticide_record TEXT NULL COMMENT '农药记录JSON',
    test_report VARCHAR(255) NULL COMMENT '检测报告URL',
    storage_condition VARCHAR(200) NOT NULL COMMENT '储存条件',
    shelf_life INT(11) NOT NULL DEFAULT 7 COMMENT '保质期（天）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源信息表';

-- --------------------------------------------------
-- 8. 果蔬圈表 (biz_circle)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_circle (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    title VARCHAR(100) NOT NULL COMMENT '动态标题',
    content TEXT NOT NULL COMMENT '动态内容',
    images TEXT NULL COMMENT '图片URLs JSON',
    product_ids VARCHAR(255) NULL COMMENT '关联商品IDs',
    view_count INT(11) NOT NULL DEFAULT 0 COMMENT '浏览量',
    like_count INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT(11) NOT NULL DEFAULT 0 COMMENT '评论数',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0草稿 1发布',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='果蔬圈表';

-- --------------------------------------------------
-- 初始化商品分类数据
-- --------------------------------------------------
INSERT INTO biz_category (parent_id, name, sort, status) VALUES
(0, '蔬菜', 1, 1),
(0, '水果', 2, 1),
(0, '菌菇', 3, 1),
(0, '其他', 4, 1),
(1, '叶菜类', 1, 1),
(1, '根茎类', 2, 1),
(1, '瓜果类', 3, 1),
(1, '豆类', 4, 1),
(2, '浆果类', 1, 1),
(2, '柑橘类', 2, 1),
(2, '核果类', 3, 1),
(2, '瓜果类', 4, 1);

-- --------------------------------------------------
-- 创建系统管理员用户（密码：123456，已加密）
-- --------------------------------------------------
INSERT INTO sys_user (username, password, phone, nickname, status) VALUES
('admin', '$2a$10$gQcDiuivi6sJxJ3.6pbNJ.uRVawT0pogiT6TCSydPkUIJSNXNXL5y', '13800138000', '系统管理员', 1);

-- --------------------------------------------------
-- 授权外键关系（可选，根据需要启用）
-- --------------------------------------------------
-- ALTER TABLE biz_merchant ADD CONSTRAINT fk_merchant_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
-- ALTER TABLE biz_product ADD CONSTRAINT fk_product_merchant FOREIGN KEY (merchant_id) REFERENCES biz_merchant(id);
-- ALTER TABLE biz_product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES biz_category(id);
-- ALTER TABLE biz_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
-- ALTER TABLE biz_order ADD CONSTRAINT fk_order_merchant FOREIGN KEY (merchant_id) REFERENCES biz_merchant(id);
-- ALTER TABLE biz_order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES biz_order(id);
-- ALTER TABLE biz_order_item ADD CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES biz_product(id);
-- ALTER TABLE biz_trace ADD CONSTRAINT fk_trace_product FOREIGN KEY (product_id) REFERENCES biz_product(id);
-- ALTER TABLE biz_circle ADD CONSTRAINT fk_circle_merchant FOREIGN KEY (merchant_id) REFERENCES biz_merchant(id);

-- --------------------------------------------------
-- 创建索引以优化查询
-- --------------------------------------------------
CREATE INDEX idx_order_create_time ON biz_order (create_time);
CREATE INDEX idx_product_create_time ON biz_product (create_time);
CREATE INDEX idx_circle_create_time ON biz_circle (create_time);

-- --------------------------------------------------
-- 显示创建结果
-- --------------------------------------------------
SELECT '数据库初始化完成！' AS message;
SELECT table_name AS '表名', table_rows AS '记录数' FROM information_schema.tables WHERE table_schema = 'fruit_veg_sales' ORDER BY table_name;
