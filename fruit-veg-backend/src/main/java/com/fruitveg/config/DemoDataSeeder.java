package com.fruitveg.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final JdbcTemplate jdbcTemplate;

    public DemoDataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            seedCategories();
            seedMerchant();
            seedProducts();
            seedTraces();
            seedBanners();
            seedNotices();
            log.info("Demo data seeded into database tables.");
        } catch (Exception e) {
            log.warn("Demo data seeding skipped: {}", e.getMessage());
        }
    }

    private void seedCategories() {
        jdbcTemplate.update(
                "INSERT INTO biz_category (id, parent_id, name, icon, sort, status, deleted, create_time, update_time) VALUES " +
                        "(1, 0, '新鲜水果', '', 1, 1, 0, NOW(), NOW())," +
                        "(2, 0, '新鲜蔬菜', '', 2, 1, 0, NOW(), NOW())," +
                        "(3, 0, '有机食品', '', 3, 1, 0, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE name=VALUES(name), sort=VALUES(sort), status=VALUES(status), deleted=0, update_time=NOW()"
        );
    }

    private void seedMerchant() {
        jdbcTemplate.update(
                "INSERT INTO biz_merchant (id, user_id, shop_name, shop_logo, shop_desc, contact_name, contact_phone, business_license, food_license, address, status, audit_time, deleted, create_time, update_time) VALUES " +
                        "(1, 1, '绿源果蔬店', '/api/images/VCG211570378418.jpg', '专注于高品质果蔬配送', '张三', '13900139000', '', '', '郑州市金水区农业路88号', 1, NOW(), 0, NOW(), NOW())," +
                        "(2, 2, '中牟鲜采果园', '/api/images/VCG211563741694.jpg', '郑州周边基地直供，当日采收配送', '李四', '13900139001', '', '', '郑州市中牟县商都路168号', 1, NOW(), 0, NOW(), NOW())," +
                        "(3, 3, '惠济绿叶仓', '/api/images/VCG211315932533.jpg', '专注叶菜和有机蔬菜冷链配送', '王五', '13900139002', '', '', '郑州市惠济区开元路66号', 1, NOW(), 0, NOW(), NOW())," +
                        "(4, 4, '果岭优选', '/api/images/VCG211327413757.jpg', '精品水果礼盒与家庭常购水果', '赵六', '13900139003', '', '', '郑州市郑东新区商务外环路99号', 1, NOW(), 0, NOW(), NOW())," +
                        "(5, 5, '南郊时蔬集', '/api/images/VCG211500639828.jpg', '南部城区时令果蔬，2小时同城达', '钱七', '13900139004', '', '', '郑州市二七区大学南路188号', 1, NOW(), 0, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE shop_name=VALUES(shop_name), shop_logo=VALUES(shop_logo), shop_desc=VALUES(shop_desc), status=1, deleted=0, update_time=NOW()"
        );
    }

    private void seedProducts() {
        jdbcTemplate.update(
                "INSERT INTO biz_product (id, merchant_id, category_id, name, main_image, images, price, original_price, stock, unit, description, sales, status, sort, deleted, create_time, update_time) VALUES " +
                        "(1,1,1,'智利车厘子2J','/api/images/VCG211490364476.webp','[\"/api/images/VCG211490364476.webp\",\"/api/images/VCG211324068414.jpg\"]',32.80,39.80,360,'500g/盒','当季海运到港，果径均匀，甜脆多汁，适合家庭尝鲜',268,1,1,0,NOW(),NOW())," +
                        "(2,1,2,'精品圣女果','/api/images/VCG211412015500.webp','[\"/api/images/VCG211412015500.webp\",\"/api/images/VCG211412015500.webp\"]',6.90,8.90,780,'500g/盒','本地温室直采，果皮薄、酸甜平衡，适合沙拉与即食',196,1,2,0,NOW(),NOW())," +
                        "(3,2,3,'有机旱黄瓜','/api/images/VCG211450687680.webp','[\"/api/images/VCG211450687680.webp\",\"/api/images/VCG211450687680.webp\"]',4.90,6.90,720,'500g/袋','通过有机种植管理，口感清脆，冷藏后风味更佳',143,1,3,0,NOW(),NOW())," +
                        "(4,4,1,'红富士苹果礼盒','/api/images/VCG211415338609.webp','[\"/api/images/VCG211415338609.webp\",\"/api/images/VCG211415338609.webp\"]',21.80,27.80,640,'2kg/箱','精选中大果，脆甜爽口，适合家庭和送礼',238,1,4,0,NOW(),NOW())," +
                        "(5,3,2,'奶油生菜','/api/images/VCG211375299502.webp','[\"/api/images/VCG211375299502.webp\",\"/api/images/VCG211375299502.webp\"]',4.80,6.20,580,'300g/份','叶片鲜嫩，适合轻食沙拉和火锅配菜',122,1,5,0,NOW(),NOW())," +
                        "(6,4,1,'麒麟西瓜小果','/api/images/VCG211583441112.webp','[\"/api/images/VCG211583441112.webp\",\"/api/images/VCG211583441112.webp\"]',18.90,24.90,240,'约2.5kg/个','单果约2.5kg，皮薄瓤红，冷藏口感更佳',175,1,6,0,NOW(),NOW())," +
                        "(7,5,3,'有机上海青','/api/images/VCG211564814308.webp','[\"/api/images/VCG211564814308.webp\",\"/api/images/VCG211564814308.webp\"]',5.80,7.80,500,'500g/袋','基地当日采收，叶梗脆嫩，适合清炒或汆烫',110,1,7,0,NOW(),NOW())," +
                        "(8,5,3,'贝贝南瓜','/api/images/VCG211429867102.webp','[\"/api/images/VCG211429867102.webp\"]',8.90,11.90,430,'900g/个','粉糯香甜，蒸烤皆宜，家庭常备食材',98,1,8,0,NOW(),NOW()) " +
                        "ON DUPLICATE KEY UPDATE merchant_id=VALUES(merchant_id), category_id=VALUES(category_id), name=VALUES(name), main_image=VALUES(main_image), images=VALUES(images), price=VALUES(price), original_price=VALUES(original_price), stock=VALUES(stock), unit=VALUES(unit), description=VALUES(description), sales=VALUES(sales), status=VALUES(status), deleted=0, update_time=NOW()"
        );
    }

    private void seedTraces() {
        jdbcTemplate.update(
                "INSERT INTO biz_trace (id, product_id, origin_name, origin_address, plant_method, plant_time, harvest_time, fertilizer_record, pesticide_record, test_report, storage_condition, shelf_life, deleted, create_time, update_time) VALUES " +
                        "(1,1,'智利中央大区果园','智利圣地亚哥周边出口果园','标准化果园管理','2026-01-05 08:00:00','2026-02-10 06:30:00','[]','[]','/api/images/VCG211404252512.jpg','0-4℃冷藏',7,0,NOW(),NOW())," +
                        "(2,2,'郑州中牟设施农业基地','郑州市中牟县现代农业示范园','绿色种植','2026-01-18 08:30:00','2026-02-24 07:00:00','[]','[]','/api/images/VCG211404252512.jpg','4-8℃冷藏',5,0,NOW(),NOW())," +
                        "(3,3,'郑州荥阳有机示范农场','郑州市荥阳市王村镇','有机种植','2026-01-20 08:00:00','2026-02-25 06:40:00','[]','[]','/api/images/VCG211404252512.jpg','4-8℃冷藏',4,0,NOW(),NOW())," +
                        "(4,4,'陕西洛川苹果产区','陕西省延安市洛川县','生态果园种植','2025-10-15 09:00:00','2026-02-18 08:20:00','[]','[]','/api/images/VCG211404252512.jpg','0-5℃冷藏',15,0,NOW(),NOW())," +
                        "(5,5,'郑州周边叶菜基地','郑州市惠济区蔬菜基地','绿色种植','2026-02-01 08:00:00','2026-02-27 06:10:00','[]','[]','/api/images/VCG211404252512.jpg','2-6℃冷藏',3,0,NOW(),NOW())," +
                        "(6,6,'开封杞县西瓜基地','河南省开封市杞县','标准化种植','2026-01-10 09:00:00','2026-02-26 06:00:00','[]','[]','/api/images/VCG211404252512.jpg','8-12℃常温阴凉',10,0,NOW(),NOW())," +
                        "(7,7,'郑州航空港有机农场','郑州航空港区绿色基地','有机种植','2026-01-25 08:20:00','2026-02-28 06:15:00','[]','[]','/api/images/VCG211404252512.jpg','2-6℃冷藏',4,0,NOW(),NOW())," +
                        "(8,8,'焦作温县南瓜基地','河南省焦作市温县','绿色种植','2026-01-12 08:30:00','2026-02-23 07:30:00','[]','[]','/api/images/VCG211404252512.jpg','8-14℃通风',20,0,NOW(),NOW()) " +
                        "ON DUPLICATE KEY UPDATE origin_name=VALUES(origin_name), origin_address=VALUES(origin_address), plant_method=VALUES(plant_method), plant_time=VALUES(plant_time), harvest_time=VALUES(harvest_time), test_report=VALUES(test_report), storage_condition=VALUES(storage_condition), shelf_life=VALUES(shelf_life), deleted=0, update_time=NOW()"
        );
    }

    private void seedBanners() {
        jdbcTemplate.update(
                "INSERT INTO biz_banner (id, title, image_url, target_url, sort, status, create_time, update_time) VALUES " +
                        "(1, '当季直采水果专场', '/api/images/VCG211357193212.jpg', '/products?category=1', 1, 1, NOW(), NOW())," +
                        "(2, '绿色蔬菜 48 小时冷链到家', '/api/images/VCG211562876467.jpg', '/products?category=2', 2, 1, NOW(), NOW())," +
                        "(3, '农残检测合格公示', '/api/images/VCG41N1414031312.jpg', '/trace/detail/1', 3, 1, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE title=VALUES(title), image_url=VALUES(image_url), target_url=VALUES(target_url), sort=VALUES(sort), status=VALUES(status), update_time=NOW()"
        );
    }

    private void seedNotices() {
        jdbcTemplate.update(
                "INSERT INTO biz_notice (id, type, title, content, publish_time, status, create_time, update_time) VALUES " +
                        "(1, '平台公告', '春节保供期间订单配送时效说明', '高峰期预计较平日延迟 12-24 小时，敬请理解。', '2026-02-26 10:00:00', 1, NOW(), NOW())," +
                        "(2, '行业资讯', '郑州本地叶菜进入最佳尝鲜期', '本周叶菜到货充足，建议优先选择当日采收批次。', '2026-02-24 09:30:00', 1, NOW(), NOW())," +
                        "(3, '平台公告', '新增 AI 果蔬专家功能，欢迎体验', '可在首页进入 AI 专家，获取选购与保鲜建议。', '2026-02-22 12:00:00', 1, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE type=VALUES(type), title=VALUES(title), content=VALUES(content), publish_time=VALUES(publish_time), status=VALUES(status), update_time=NOW()"
        );
    }
}
