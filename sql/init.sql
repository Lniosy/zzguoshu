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
-- 9. 首页轮播表 (biz_banner)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_banner (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    target_url VARCHAR(500) NULL COMMENT '跳转链接',
    sort INT(11) NULL DEFAULT 0 COMMENT '排序',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播表';

-- --------------------------------------------------
-- 10. 公告表 (biz_notice)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_notice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    type VARCHAR(50) NOT NULL COMMENT '公告类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content VARCHAR(1000) NULL COMMENT '正文',
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

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
-- 初始化商家、商品、溯源、首页内容
-- --------------------------------------------------
INSERT INTO biz_merchant (id, user_id, shop_name, shop_logo, shop_desc, contact_name, contact_phone, business_license, food_license, address, status, audit_time, create_time, update_time)
VALUES
(1, 1, '绿源果蔬店', 'https://images.unsplash.com/photo-1685273899682-fiVn6KQhIPo?auto=format&fit=crop&w=1200&q=80', '专注于高品质果蔬配送', '张三', '13900139000', '', '', '郑州市金水区农业路88号', 1, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE shop_name=VALUES(shop_name), shop_logo=VALUES(shop_logo), shop_desc=VALUES(shop_desc), status=1, update_time=NOW();

INSERT INTO biz_product (id, merchant_id, category_id, name, main_image, images, price, original_price, stock, unit, description, sales, status, sort, create_time, update_time)
VALUES
(1,1,2,'智利车厘子2J','https://images.unsplash.com/photo-1498557850523-fd3d118b962e?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1498557850523-fd3d118b962e?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1642752602324-jBZSdHbS4Rg?auto=format&fit=crop&w=1200&q=80"]',39.90,49.90,360,'500g/盒','当季海运到港，果径均匀，甜脆多汁，适合家庭尝鲜',268,1,1,NOW(),NOW()),
(2,1,1,'精品圣女果','https://images.unsplash.com/photo-1755321096149-v2b9Zh1ORm8?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1765653379054-88OzijJfpv8?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1742124768458-agXVu0ILIOU?auto=format&fit=crop&w=1200&q=80"]',8.80,12.80,780,'500g/盒','本地温室直采，果皮薄、酸甜平衡，适合沙拉与即食',196,1,2,NOW(),NOW()),
(3,1,1,'有机旱黄瓜','https://images.unsplash.com/photo-1568584711271-096ca3376931?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1568584711271-096ca3376931?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1737558596633-VqMW9OwAwig?auto=format&fit=crop&w=1200&q=80"]',6.90,8.90,720,'500g/袋','通过有机种植管理，口感清脆，冷藏后风味更佳',143,1,3,NOW(),NOW()),
(4,1,2,'红富士苹果礼盒','https://images.unsplash.com/photo-1652299035422-7waRESfE464?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1652299035422-7waRESfE464?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1566737236500-c8ac43014a67?auto=format&fit=crop&w=1200&q=80"]',19.80,25.80,640,'2kg/箱','精选中大果，脆甜爽口，适合家庭和送礼',238,1,4,NOW(),NOW()),
(5,1,1,'奶油生菜','https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1659466100158-PchPR1WcE1Q?auto=format&fit=crop&w=1200&q=80"]',5.60,7.20,580,'300g/份','叶片鲜嫩，适合轻食沙拉和火锅配菜',122,1,5,NOW(),NOW()),
(6,1,2,'麒麟西瓜小果','https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1615485925879-9729b59f5d62?auto=format&fit=crop&w=1200&q=80"]',29.90,36.00,240,'约2.5kg/个','单果约2.5kg，皮薄瓤红，冷藏口感更佳',175,1,6,NOW(),NOW()),
(7,1,1,'有机上海青','https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=1200&q=80","https://images.unsplash.com/photo-1709402812245-wguVC8oG3qw?auto=format&fit=crop&w=1200&q=80"]',7.50,9.80,500,'500g/袋','基地当日采收，叶梗脆嫩，适合清炒或汆烫',110,1,7,NOW(),NOW()),
(8,1,4,'贝贝南瓜','https://images.unsplash.com/photo-1570586437263-ab629fccc818?auto=format&fit=crop&w=1200&q=80','["https://images.unsplash.com/photo-1570586437263-ab629fccc818?auto=format&fit=crop&w=1200&q=80"]',12.90,16.90,430,'900g/个','粉糯香甜，蒸烤皆宜，家庭常备食材',98,1,8,NOW(),NOW())
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id), name=VALUES(name), main_image=VALUES(main_image), images=VALUES(images), price=VALUES(price), original_price=VALUES(original_price), stock=VALUES(stock), unit=VALUES(unit), description=VALUES(description), sales=VALUES(sales), status=VALUES(status), update_time=NOW();

INSERT INTO biz_trace (id, product_id, origin_name, origin_address, plant_method, plant_time, harvest_time, fertilizer_record, pesticide_record, test_report, storage_condition, shelf_life, create_time, update_time)
VALUES
(1,1,'智利中央大区果园','智利圣地亚哥周边出口果园','标准化果园管理','2026-01-05 08:00:00','2026-02-10 06:30:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','0-4℃冷藏',7,NOW(),NOW()),
(2,2,'郑州中牟设施农业基地','郑州市中牟县现代农业示范园','绿色种植','2026-01-18 08:30:00','2026-02-24 07:00:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','4-8℃冷藏',5,NOW(),NOW()),
(3,3,'郑州荥阳有机示范农场','郑州市荥阳市王村镇','有机种植','2026-01-20 08:00:00','2026-02-25 06:40:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','4-8℃冷藏',4,NOW(),NOW()),
(4,4,'陕西洛川苹果产区','陕西省延安市洛川县','生态果园种植','2025-10-15 09:00:00','2026-02-18 08:20:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','0-5℃冷藏',15,NOW(),NOW()),
(5,5,'郑州周边叶菜基地','郑州市惠济区蔬菜基地','绿色种植','2026-02-01 08:00:00','2026-02-27 06:10:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','2-6℃冷藏',3,NOW(),NOW()),
(6,6,'开封杞县西瓜基地','河南省开封市杞县','标准化种植','2026-01-10 09:00:00','2026-02-26 06:00:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','8-12℃常温阴凉',10,NOW(),NOW()),
(7,7,'郑州航空港有机农场','郑州航空港区绿色基地','有机种植','2026-01-25 08:20:00','2026-02-28 06:15:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','2-6℃冷藏',4,NOW(),NOW()),
(8,8,'焦作温县南瓜基地','河南省焦作市温县','绿色种植','2026-01-12 08:30:00','2026-02-23 07:30:00','[]','[]','https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1200&q=80','8-14℃通风',20,NOW(),NOW())
ON DUPLICATE KEY UPDATE origin_name=VALUES(origin_name), origin_address=VALUES(origin_address), plant_method=VALUES(plant_method), plant_time=VALUES(plant_time), harvest_time=VALUES(harvest_time), test_report=VALUES(test_report), storage_condition=VALUES(storage_condition), shelf_life=VALUES(shelf_life), update_time=NOW();

INSERT INTO biz_banner (id, title, image_url, target_url, sort, status, create_time, update_time)
VALUES
(1, '当季直采水果专场', 'https://images.unsplash.com/photo-1498557850523-fd3d118b962e?auto=format&fit=crop&w=1600&q=80', '/products?category=1', 1, 1, NOW(), NOW()),
(2, '绿色蔬菜 48 小时冷链到家', 'https://images.unsplash.com/photo-1709402812245-wguVC8oG3qw?auto=format&fit=crop&w=1600&q=80', '/products?category=2', 2, 1, NOW(), NOW()),
(3, '农残检测合格公示', 'https://images.unsplash.com/photo-1690293067872-pjcoyZLSnpw?auto=format&fit=crop&w=1600&q=80', '/trace/detail/1', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), image_url=VALUES(image_url), target_url=VALUES(target_url), sort=VALUES(sort), status=VALUES(status), update_time=NOW();

INSERT INTO biz_notice (id, type, title, content, publish_time, status, create_time, update_time)
VALUES
(1, '平台公告', '春节保供期间订单配送时效说明', '高峰期预计较平日延迟 12-24 小时，敬请理解。', '2026-02-26 10:00:00', 1, NOW(), NOW()),
(2, '行业资讯', '郑州本地叶菜进入最佳尝鲜期', '本周叶菜到货充足，建议优先选择当日采收批次。', '2026-02-24 09:30:00', 1, NOW(), NOW()),
(3, '平台公告', '新增 AI 果蔬专家功能，欢迎体验', '可在首页进入 AI 专家，获取选购与保鲜建议。', '2026-02-22 12:00:00', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE type=VALUES(type), title=VALUES(title), content=VALUES(content), publish_time=VALUES(publish_time), status=VALUES(status), update_time=NOW();

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
