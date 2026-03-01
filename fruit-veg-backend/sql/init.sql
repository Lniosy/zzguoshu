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
-- 首页轮播表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    target_url VARCHAR(500) COMMENT '跳转链接',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播表';

-- ==============================================
-- 公告表
-- ==============================================
CREATE TABLE IF NOT EXISTS biz_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(50) NOT NULL COMMENT '公告类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content VARCHAR(1000) COMMENT '正文',
    publish_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ==============================================
-- 初始化数据
-- ==============================================

-- 插入默认管理员用户（密码：123456，已加密）
INSERT INTO sys_user (username, password, phone, nickname, avatar, gender, status, create_time)
VALUES ('admin', '$2a$10$7JB720yubVSdLb.CiXH2ehema5dH3tqqK50tZxW9j8nP1uF12K3qQ', '13800138000', '管理员', '', 0, 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 插入默认分类
INSERT INTO biz_category (id, parent_id, name, icon, sort, status, create_time, update_time, deleted)
VALUES
(1, 0, '新鲜水果', '/images/category/fruit.png', 1, 1, NOW(), NOW(), 0),
(2, 0, '新鲜蔬菜', '/images/category/vegetable.png', 2, 1, NOW(), NOW(), 0),
(3, 0, '有机食品', '/images/category/organic.png', 3, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), sort=VALUES(sort), status=VALUES(status), deleted=0, update_time=NOW();

-- 插入测试商家
INSERT INTO biz_merchant (id, user_id, shop_name, shop_logo, shop_desc, contact_name, contact_phone, business_license, food_license, address, status, audit_time, create_time, update_time, deleted)
VALUES
(1, 1, '绿源果蔬店', '/api/images/VCG211570378418.jpg', '专注于高品质果蔬配送', '张三', '13900139000', '', '', '郑州市金水区农业路88号', 1, NOW(), NOW(), NOW(), 0),
(2, 2, '中牟鲜采果园', '/api/images/VCG211563741694.jpg', '郑州周边基地直供，当日采收配送', '李四', '13900139001', '', '', '郑州市中牟县商都路168号', 1, NOW(), NOW(), NOW(), 0),
(3, 3, '惠济绿叶仓', '/api/images/VCG211315932533.jpg', '专注叶菜和有机蔬菜冷链配送', '王五', '13900139002', '', '', '郑州市惠济区开元路66号', 1, NOW(), NOW(), NOW(), 0),
(4, 4, '果岭优选', '/api/images/VCG211327413757.jpg', '精品水果礼盒与家庭常购水果', '赵六', '13900139003', '', '', '郑州市郑东新区商务外环路99号', 1, NOW(), NOW(), NOW(), 0),
(5, 5, '南郊时蔬集', '/api/images/VCG211500639828.jpg', '南部城区时令果蔬，2小时同城达', '钱七', '13900139004', '', '', '郑州市二七区大学南路188号', 1, NOW(), NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE shop_name=VALUES(shop_name), shop_logo=VALUES(shop_logo), shop_desc=VALUES(shop_desc), status=1, deleted=0, update_time=NOW();

-- 插入测试商品
INSERT INTO biz_product (id, merchant_id, category_id, name, main_image, images, price, original_price, stock, unit, description, sales, status, sort, create_time, update_time, deleted)
VALUES
(1,1,1,'智利车厘子2J','/api/images/VCG211490364476.webp','["/api/images/VCG211490364476.webp","/api/images/VCG211324068414.jpg"]',32.80,39.80,360,'500g/盒','当季海运到港，果径均匀，甜脆多汁，适合家庭尝鲜',268,1,1,NOW(),NOW(),0),
(2,1,2,'精品圣女果','/api/images/VCG211412015500.webp','["/api/images/VCG211412015500.webp","/api/images/VCG211412015500.webp"]',6.90,8.90,780,'500g/盒','本地温室直采，果皮薄、酸甜平衡，适合沙拉与即食',196,1,2,NOW(),NOW(),0),
(3,2,3,'有机旱黄瓜','/api/images/VCG211450687680.webp','["/api/images/VCG211450687680.webp","/api/images/VCG211450687680.webp"]',4.90,6.90,720,'500g/袋','通过有机种植管理，口感清脆，冷藏后风味更佳',143,1,3,NOW(),NOW(),0),
(4,4,1,'红富士苹果礼盒','/api/images/VCG211415338609.webp','["/api/images/VCG211415338609.webp","/api/images/VCG211415338609.webp"]',21.80,27.80,640,'2kg/箱','精选中大果，脆甜爽口，适合家庭和送礼',238,1,4,NOW(),NOW(),0),
(5,3,2,'奶油生菜','/api/images/VCG211375299502.webp','["/api/images/VCG211375299502.webp","/api/images/VCG211375299502.webp"]',4.80,6.20,580,'300g/份','叶片鲜嫩，适合轻食沙拉和火锅配菜',122,1,5,NOW(),NOW(),0),
(6,4,1,'麒麟西瓜小果','/api/images/VCG211583441112.webp','["/api/images/VCG211583441112.webp","/api/images/VCG211583441112.webp"]',18.90,24.90,240,'约2.5kg/个','单果约2.5kg，皮薄瓤红，冷藏口感更佳',175,1,6,NOW(),NOW(),0),
(7,5,3,'有机上海青','/api/images/VCG211564814308.webp','["/api/images/VCG211564814308.webp","/api/images/VCG211564814308.webp"]',5.80,7.80,500,'500g/袋','基地当日采收，叶梗脆嫩，适合清炒或汆烫',110,1,7,NOW(),NOW(),0),
(8,5,3,'贝贝南瓜','/api/images/VCG211429867102.webp','["/api/images/VCG211429867102.webp"]',8.90,11.90,430,'900g/个','粉糯香甜，蒸烤皆宜，家庭常备食材',98,1,8,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE merchant_id=VALUES(merchant_id), category_id=VALUES(category_id), name=VALUES(name), main_image=VALUES(main_image), images=VALUES(images), price=VALUES(price), original_price=VALUES(original_price), stock=VALUES(stock), unit=VALUES(unit), description=VALUES(description), sales=VALUES(sales), status=VALUES(status), deleted=0, update_time=NOW();

-- 插入测试溯源
INSERT INTO biz_trace (id, product_id, origin_name, origin_address, plant_method, plant_time, harvest_time, fertilizer_record, pesticide_record, test_report, storage_condition, shelf_life, deleted, create_time, update_time)
VALUES
(1,1,'智利中央大区果园','智利圣地亚哥周边出口果园','标准化果园管理','2026-01-05 08:00:00','2026-02-10 06:30:00','[]','[]','/api/images/VCG211404252512.jpg','0-4℃冷藏',7,0,NOW(),NOW()),
(2,2,'郑州中牟设施农业基地','郑州市中牟县现代农业示范园','绿色种植','2026-01-18 08:30:00','2026-02-24 07:00:00','[]','[]','/api/images/VCG211404252512.jpg','4-8℃冷藏',5,0,NOW(),NOW()),
(3,3,'郑州荥阳有机示范农场','郑州市荥阳市王村镇','有机种植','2026-01-20 08:00:00','2026-02-25 06:40:00','[]','[]','/api/images/VCG211404252512.jpg','4-8℃冷藏',4,0,NOW(),NOW()),
(4,4,'陕西洛川苹果产区','陕西省延安市洛川县','生态果园种植','2025-10-15 09:00:00','2026-02-18 08:20:00','[]','[]','/api/images/VCG211404252512.jpg','0-5℃冷藏',15,0,NOW(),NOW()),
(5,5,'郑州周边叶菜基地','郑州市惠济区蔬菜基地','绿色种植','2026-02-01 08:00:00','2026-02-27 06:10:00','[]','[]','/api/images/VCG211404252512.jpg','2-6℃冷藏',3,0,NOW(),NOW()),
(6,6,'开封杞县西瓜基地','河南省开封市杞县','标准化种植','2026-01-10 09:00:00','2026-02-26 06:00:00','[]','[]','/api/images/VCG211404252512.jpg','8-12℃常温阴凉',10,0,NOW(),NOW()),
(7,7,'郑州航空港有机农场','郑州航空港区绿色基地','有机种植','2026-01-25 08:20:00','2026-02-28 06:15:00','[]','[]','/api/images/VCG211404252512.jpg','2-6℃冷藏',4,0,NOW(),NOW()),
(8,8,'焦作温县南瓜基地','河南省焦作市温县','绿色种植','2026-01-12 08:30:00','2026-02-23 07:30:00','[]','[]','/api/images/VCG211404252512.jpg','8-14℃通风',20,0,NOW(),NOW())
ON DUPLICATE KEY UPDATE origin_name=VALUES(origin_name), origin_address=VALUES(origin_address), plant_method=VALUES(plant_method), plant_time=VALUES(plant_time), harvest_time=VALUES(harvest_time), test_report=VALUES(test_report), storage_condition=VALUES(storage_condition), shelf_life=VALUES(shelf_life), deleted=0, update_time=NOW();

-- 插入首页轮播
INSERT INTO biz_banner (id, title, image_url, target_url, sort, status, create_time, update_time)
VALUES
(1, '当季直采水果专场', '/api/images/VCG211357193212.jpg', '/products?category=1', 1, 1, NOW(), NOW()),
(2, '绿色蔬菜 48 小时冷链到家', '/api/images/VCG211562876467.jpg', '/products?category=2', 2, 1, NOW(), NOW()),
(3, '农残检测合格公示', '/api/images/VCG41N1414031312.jpg', '/trace/detail/1', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), image_url=VALUES(image_url), target_url=VALUES(target_url), sort=VALUES(sort), status=VALUES(status), update_time=NOW();

-- 插入公告
INSERT INTO biz_notice (id, type, title, content, publish_time, status, create_time, update_time)
VALUES
(1, '平台公告', '春节保供期间订单配送时效说明', '高峰期预计较平日延迟 12-24 小时，敬请理解。', '2026-02-26 10:00:00', 1, NOW(), NOW()),
(2, '行业资讯', '郑州本地叶菜进入最佳尝鲜期', '本周叶菜到货充足，建议优先选择当日采收批次。', '2026-02-24 09:30:00', 1, NOW(), NOW()),
(3, '平台公告', '新增 AI 果蔬专家功能，欢迎体验', '可在首页进入 AI 专家，获取选购与保鲜建议。', '2026-02-22 12:00:00', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE type=VALUES(type), title=VALUES(title), content=VALUES(content), publish_time=VALUES(publish_time), status=VALUES(status), update_time=NOW();

-- ==============================================
-- 结束初始化
-- ==============================================
