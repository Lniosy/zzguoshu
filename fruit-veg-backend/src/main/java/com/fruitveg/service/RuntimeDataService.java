package com.fruitveg.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RuntimeDataService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATE_KEY = "runtime-data-service-v2";
    private static final Pattern LEGACY_VCG_PATTERN =
            Pattern.compile(".*/(VCG[0-9A-Za-z]+\\.(?:jpg|jpeg))(?:\\?.*)?$", Pattern.CASE_INSENSITIVE);
    private static final Map<String, String> IMAGE_URLS = new HashMap<>();

    static {
        IMAGE_URLS.put("shop", "/api/images/VCG211570378418.jpg");

        IMAGE_URLS.put("banner-1", "/api/images/VCG211357193212.jpg");
        IMAGE_URLS.put("banner-2", "/api/images/VCG211562876467.jpg");
        IMAGE_URLS.put("banner-3", "/api/images/VCG41N1414031312.jpg");

        IMAGE_URLS.put("circle-1", "/api/images/VCG211485231852.webp");
        IMAGE_URLS.put("circle-2", "/api/images/VCG41N1414031312.webp");
        IMAGE_URLS.put("circle-3", "/api/images/VCG41N1413254862.webp");

        IMAGE_URLS.put("tomato", "/api/images/VCG41N1487022738.jpg");
        IMAGE_URLS.put("apple", "/api/images/VCG211554725298.jpg");

        IMAGE_URLS.put("product1", "/api/images/VCG211490364476.webp");
        IMAGE_URLS.put("product-detail1", "/api/images/VCG211324068414.jpg");
        IMAGE_URLS.put("product2", "/api/images/VCG211412015500.webp");
        IMAGE_URLS.put("product-detail2", "/api/images/VCG211412015500.webp");
        IMAGE_URLS.put("product3", "/api/images/VCG211450687680.webp");
        IMAGE_URLS.put("product-detail3", "/api/images/VCG211450687680.webp");
        IMAGE_URLS.put("product4", "/api/images/VCG211415338609.webp");
        IMAGE_URLS.put("product-detail4", "/api/images/VCG211415338609.webp");
        IMAGE_URLS.put("product5", "/api/images/VCG211375299502.webp");
        IMAGE_URLS.put("product-detail5", "/api/images/VCG211375299502.webp");
        IMAGE_URLS.put("product6", "/api/images/VCG211583441112.webp");
        IMAGE_URLS.put("product-detail6", "/api/images/VCG211583441112.webp");
        IMAGE_URLS.put("product7", "/api/images/VCG211564814308.webp");
        IMAGE_URLS.put("product-detail7", "/api/images/VCG211564814308.webp");
        IMAGE_URLS.put("product8", "/api/images/VCG211429867102.webp");
        IMAGE_URLS.put("product-detail8", "/api/images/VCG211429867102.webp");
    }

    private final List<Map<String, Object>> categories = new ArrayList<>();
    private final List<Map<String, Object>> products = new ArrayList<>();
    private final List<Map<String, Object>> merchants = new ArrayList<>();
    private final List<Map<String, Object>> userOrders = new ArrayList<>();
    private final List<Map<String, Object>> complaints = new ArrayList<>();
    private final List<Map<String, Object>> banners = new ArrayList<>();
    private final List<Map<String, Object>> notices = new ArrayList<>();
    private final List<Map<String, Object>> circlePosts = new ArrayList<>();
    private final List<Map<String, Object>> traceTemplates = new ArrayList<>();
    private final Map<Long, Map<String, Object>> traceByProduct = new ConcurrentHashMap<>();
    private final Map<Long, List<Map<String, Object>>> aiHistoryByUser = new ConcurrentHashMap<>();
    private final Map<Long, Long> aiActiveSessionByUser = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> favoriteProductIdsByUser = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> followMerchantIdsByUser = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> orderReviews = new ArrayList<>();
    private final Map<String, Object> systemSettings = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rolePermissions = new ConcurrentHashMap<>();
    private final AtomicLong orderIdSeq = new AtomicLong(1000);
    private final AtomicLong merchantIdSeq = new AtomicLong(100);
    private final AtomicLong circlePostIdSeq = new AtomicLong(10);
    private final AtomicLong complaintIdSeq = new AtomicLong(2000);
    private final AtomicLong productIdSeq = new AtomicLong(1000);
    private final AtomicLong bannerIdSeq = new AtomicLong(1000);
    private final AtomicLong noticeIdSeq = new AtomicLong(1000);
    private final AtomicLong traceTemplateIdSeq = new AtomicLong(1000);
    private final AtomicLong aiSessionIdSeq = new AtomicLong(5000);
    private final AtomicLong aiMessageIdSeq = new AtomicLong(10000);
    private final Map<Long, Map<String, Object>> merchantByUserId = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public RuntimeDataService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (loadStateFromDb()) {
            return;
        }
        if (!categories.isEmpty()) {
            return;
        }

        categories.add(category(1L, 0L, "新鲜水果"));
        categories.add(category(2L, 0L, "新鲜蔬菜"));
        categories.add(category(3L, 0L, "有机食品"));

        products.add(product(1L, 1L, "智利车厘子2J", "应季进口，果径均匀，甜脆多汁，适合家庭鲜食", 32.80, 39.80, "500g/盒", 360, 268, 1));
        products.add(product(2L, 2L, "精品圣女果", "本地温室采收，酸甜平衡，适合沙拉与即食", 6.90, 8.90, "500g/盒", 780, 196, 1));
        products.add(product(3L, 3L, "有机旱黄瓜", "脆嫩清爽，凉拌和热炒都适合", 4.90, 6.90, "500g/袋", 720, 143, 1));
        products.add(product(4L, 1L, "红富士苹果礼盒", "中大果混装，脆甜多汁，家用与送礼皆可", 21.80, 27.80, "2kg/箱", 640, 238, 1));
        products.add(product(5L, 2L, "奶油生菜", "叶片鲜嫩，轻食沙拉和火锅都适配", 4.80, 6.20, "300g/份", 580, 122, 1));
        products.add(product(6L, 1L, "麒麟西瓜小果", "单果约2.5kg，皮薄瓤红，口感清甜", 18.90, 24.90, "约2.5kg/个", 240, 175, 1));
        products.add(product(7L, 3L, "有机上海青", "当天采收，叶梗脆嫩，适合清炒汆烫", 5.80, 7.80, "500g/袋", 500, 110, 1));
        products.add(product(8L, 3L, "贝贝南瓜", "粉糯香甜，蒸烤炖煮均可", 8.90, 11.90, "900g/个", 430, 98, 1));

        Map<String, Object> defaultMerchant = new LinkedHashMap<>();
        defaultMerchant.put("id", 1L);
        defaultMerchant.put("name", "绿源果蔬店");
        defaultMerchant.put("shopName", "绿源果蔬店");
        defaultMerchant.put("businessType", "FRESH_RETAIL");
        defaultMerchant.put("shopDescription", "专注于高品质果蔬配送");
        defaultMerchant.put("contactName", "张三");
        defaultMerchant.put("contactPhone", "13900139000");
        defaultMerchant.put("address", "郑州市金水区农业路88号");
        defaultMerchant.put("logoUrl", image("shop"));
        defaultMerchant.put("auditStatus", 1);
        defaultMerchant.put("createTime", now());
        merchants.add(defaultMerchant);
        merchantByUserId.put(1L, defaultMerchant);

        userOrders.add(sampleOrder(1L, "pending", 0));
        userOrders.add(sampleOrder(2L, "delivered", 2));
        userOrders.add(sampleOrder(3L, "completed", 4));

        banners.add(banner(1L, "当季直采水果专场", image("banner-1"), "/products?category=1"));
        banners.add(banner(2L, "绿色蔬菜 48 小时冷链到家", image("banner-2"), "/products?category=2"));
        banners.add(banner(3L, "农残检测合格公示", image("banner-3"), "/trace/detail/1"));

        notices.add(notice(1L, "平台公告", "春节保供期间订单配送时效说明", "2026-02-26 10:00:00"));
        notices.add(notice(2L, "行业资讯", "郑州本地叶菜进入最佳尝鲜期", "2026-02-24 09:30:00"));
        notices.add(notice(3L, "平台公告", "新增 AI 果蔬专家功能，欢迎体验", "2026-02-22 12:00:00"));

        systemSettings.put("platformName", "郑州市果蔬销售系统");
        systemSettings.put("contactPhone", "400-800-9090");
        systemSettings.put("contactEmail", "service@fruitveg.local");
        systemSettings.put("icpNo", "豫ICP备2026XXXX号");
        systemSettings.put("copyright", "© 2026 郑州市果蔬销售系统");
        systemSettings.put("orderAutoCancelMinutes", 30);
        systemSettings.put("afterSaleExpireDays", 7);
        systemSettings.put("productAuditRequired", true);
        systemSettings.put("traceAuditRequired", false);
        rolePermissions.put("ADMIN", new LinkedHashSet<>(Arrays.asList(
                "admin:users:view", "admin:users:status", "admin:users:export",
                "admin:products:view", "admin:products:audit",
                "admin:orders:view", "admin:orders:ship",
                "admin:complaints:view", "admin:complaints:handle",
                "admin:merchants:view", "admin:merchants:audit",
                "admin:content:manage", "admin:settings:manage", "admin:roles:manage",
                "admin:trace:template", "admin:trace:audit", "admin:trace:stats"
        )));
        rolePermissions.put("MERCHANT", new LinkedHashSet<>(Arrays.asList(
                "merchant:shop:view", "merchant:shop:edit",
                "merchant:products:manage", "merchant:trace:manage",
                "merchant:orders:ship", "merchant:circle:manage", "merchant:stats:view"
        )));
        rolePermissions.put("USER", new LinkedHashSet<>(Arrays.asList(
                "user:profile:manage", "user:address:manage",
                "user:order:view", "user:after-sale:apply",
                "user:circle:interact", "user:favorite:manage"
        )));

        productIdSeq.set(products.stream().mapToLong(p -> ((Number) p.get("id")).longValue()).max().orElse(1000L));
        bannerIdSeq.set(banners.stream().mapToLong(b -> ((Number) b.get("id")).longValue()).max().orElse(1000L));
        noticeIdSeq.set(notices.stream().mapToLong(n -> ((Number) n.get("id")).longValue()).max().orElse(1000L));

        traceTemplates.add(traceTemplate(1L, "基础溯源模板", "适用于水果/蔬菜标准溯源"));
        traceTemplates.add(traceTemplate(2L, "有机食品模板", "适用于有机产品，包含认证字段"));
        traceTemplateIdSeq.set(traceTemplates.stream().mapToLong(t -> ((Number) t.get("id")).longValue()).max().orElse(1000L));

        for (Map<String, Object> product : products) {
            Long productId = ((Number) product.get("id")).longValue();
            traceByProduct.put(productId, trace(productId, String.valueOf(product.get("name"))));
        }

        circlePosts.add(circlePost(1L, "今日到货：智利车厘子", "凌晨分拣入库，上午完成冷链发车，现货充足。", Arrays.asList(image("circle-1"), image("circle-2")), Arrays.asList(1L, 4L)));
        circlePosts.add(circlePost(2L, "有机绿叶菜采收记录", "上海青与生菜已完成农残抽检，下午批次可下单。", Collections.singletonList(image("circle-3")), Arrays.asList(7L, 5L)));
        persistState();
    }

    public List<Map<String, Object>> listCategoryTree() {
        return categories.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>(c);
            item.put("children", new ArrayList<>());
            return item;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> listProducts(Integer page, Integer size, String keyword, String sortBy, Long categoryId,
                                            Double minPrice, Double maxPrice) {
        List<Map<String, Object>> filtered = products.stream()
                .filter(p -> keyword == null
                        || String.valueOf(p.get("name")).contains(keyword)
                        || getMerchantNameById(((Number) p.getOrDefault("merchantId", 1L)).longValue()).contains(keyword))
                .filter(p -> categoryId == null || Objects.equals(((Number) p.get("categoryId")).longValue(), categoryId))
                .filter(p -> minPrice == null || ((Number) p.get("price")).doubleValue() >= minPrice)
                .filter(p -> maxPrice == null || ((Number) p.get("price")).doubleValue() <= maxPrice)
                .collect(Collectors.toList());

        if ("price_asc".equals(sortBy)) {
            filtered.sort(Comparator.comparingDouble(p -> ((Number) p.get("price")).doubleValue()));
        } else if ("price_desc".equals(sortBy)) {
            filtered.sort((a, b) -> Double.compare(((Number) b.get("price")).doubleValue(), ((Number) a.get("price")).doubleValue()));
        } else if ("sales_desc".equals(sortBy)) {
            filtered.sort((a, b) -> Integer.compare(((Number) b.get("sales")).intValue(), ((Number) a.get("sales")).intValue()));
        }

        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 12 : size;
        int from = Math.min((p - 1) * s, filtered.size());
        int to = Math.min(from + s, filtered.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", filtered.subList(from, to));
        result.put("total", filtered.size());
        result.put("current", p);
        result.put("size", s);
        return result;
    }

    public List<Map<String, Object>> listBanners() {
        return banners.stream().map(LinkedHashMap::new).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listNotices() {
        return notices.stream()
                .sorted((a, b) -> String.valueOf(b.get("publishTime")).compareTo(String.valueOf(a.get("publishTime"))))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
    }

    public Map<String, Object> listAdminBanners(Integer page, Integer pageSize, String title) {
        List<Map<String, Object>> rows = banners.stream()
                .filter(b -> title == null || title.isEmpty() || String.valueOf(b.get("title")).contains(title))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> saveBanner(Map<String, Object> payload) {
        Long id = toLong(payload.get("id"));
        Map<String, Object> row;
        if (id == null) {
            row = new LinkedHashMap<>();
            row.put("id", bannerIdSeq.incrementAndGet());
            banners.add(0, row);
        } else {
            row = banners.stream().filter(b -> Objects.equals(((Number) b.get("id")).longValue(), id)).findFirst().orElse(null);
            if (row == null) {
                row = new LinkedHashMap<>();
                row.put("id", id);
                banners.add(0, row);
            }
        }
        row.put("title", String.valueOf(payload.getOrDefault("title", "平台轮播")));
        row.put("imageUrl", String.valueOf(payload.getOrDefault("imageUrl", image("banner-custom-" + row.get("id")))));
        row.put("targetUrl", String.valueOf(payload.getOrDefault("targetUrl", "/")));
        return new LinkedHashMap<>(row);
    }

    public boolean deleteBanner(Long id) {
        return banners.removeIf(b -> Objects.equals(((Number) b.get("id")).longValue(), id));
    }

    public Map<String, Object> listAdminNotices(Integer page, Integer pageSize, String type, String title) {
        List<Map<String, Object>> rows = notices.stream()
                .filter(n -> type == null || type.isEmpty() || Objects.equals(String.valueOf(n.get("type")), type))
                .filter(n -> title == null || title.isEmpty() || String.valueOf(n.get("title")).contains(title))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> saveNotice(Map<String, Object> payload) {
        Long id = toLong(payload.get("id"));
        Map<String, Object> row;
        if (id == null) {
            row = new LinkedHashMap<>();
            row.put("id", noticeIdSeq.incrementAndGet());
            notices.add(0, row);
        } else {
            row = notices.stream().filter(n -> Objects.equals(((Number) n.get("id")).longValue(), id)).findFirst().orElse(null);
            if (row == null) {
                row = new LinkedHashMap<>();
                row.put("id", id);
                notices.add(0, row);
            }
        }
        row.put("type", String.valueOf(payload.getOrDefault("type", "平台公告")));
        row.put("title", String.valueOf(payload.getOrDefault("title", "平台公告")));
        row.put("content", String.valueOf(payload.getOrDefault("content", "请完善公告正文内容。")));
        row.put("publishTime", now());
        return new LinkedHashMap<>(row);
    }

    public boolean deleteNotice(Long id) {
        return notices.removeIf(n -> Objects.equals(((Number) n.get("id")).longValue(), id));
    }

    public Map<String, Object> getSystemSettings() {
        return new LinkedHashMap<>(systemSettings);
    }

    public Map<String, Object> updateSystemSettings(Map<String, Object> payload) {
        if (payload != null) {
            payload.forEach((k, v) -> {
                if (v != null) {
                    systemSettings.put(k, v);
                }
            });
        }
        systemSettings.put("updateTime", now());
        return new LinkedHashMap<>(systemSettings);
    }

    public List<Map<String, Object>> listRoles() {
        return rolePermissions.entrySet().stream().map(entry -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("role", entry.getKey());
            row.put("permissions", new ArrayList<>(entry.getValue()));
            row.put("permissionCount", entry.getValue().size());
            return row;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> updateRolePermissions(String role, List<String> permissions) {
        if (!rolePermissions.containsKey(role)) {
            return null;
        }
        Set<String> normalized = permissions == null
                ? new LinkedHashSet<>()
                : permissions.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        rolePermissions.put(role, normalized);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("role", role);
        row.put("permissions", new ArrayList<>(normalized));
        row.put("permissionCount", normalized.size());
        row.put("updateTime", now());
        return row;
    }

    public List<Map<String, Object>> recommendProducts() {
        return products.stream().filter(p -> ((Number) p.get("auditStatus")).intValue() == 1).limit(6).map(this::toHomeProduct).collect(Collectors.toList());
    }

    public Map<String, Object> getProduct(Long id) {
        Map<String, Object> p = products.stream()
                .filter(item -> Objects.equals(((Number) item.get("id")).longValue(), id))
                .findFirst()
                .orElse(null);
        if (p == null) {
            return null;
        }

        Map<String, Object> detail = new LinkedHashMap<>(p);
        detail.put("specs", Arrays.asList(
                spec(1L, "500g/盒", ((Number) p.get("price")).doubleValue()),
                spec(2L, "1kg/盒", ((Number) p.get("price")).doubleValue() * 1.88)
        ));
        detail.put("shop", merchants.get(0));
        return detail;
    }

    public List<Map<String, Object>> listUserFavorites(Long userId) {
        Set<Long> ids = favoriteProductIdsByUser.getOrDefault(userId, Collections.emptySet());
        return products.stream()
                .filter(p -> ids.contains(((Number) p.get("id")).longValue()))
                .map(this::toHomeProduct)
                .collect(Collectors.toList());
    }

    public boolean addUserFavorite(Long userId, Long productId) {
        boolean exists = products.stream().anyMatch(p -> Objects.equals(((Number) p.get("id")).longValue(), productId));
        if (!exists) return false;
        favoriteProductIdsByUser.computeIfAbsent(userId, k -> new LinkedHashSet<>()).add(productId);
        return true;
    }

    public boolean removeUserFavorite(Long userId, Long productId) {
        Set<Long> ids = favoriteProductIdsByUser.get(userId);
        return ids != null && ids.remove(productId);
    }

    public Map<String, Object> listMerchantProducts(Long userId, Integer page, Integer pageSize, String name, Integer auditStatus) {
        Long merchantId = getMerchantIdByUserId(userId);
        List<Map<String, Object>> rows = products.stream()
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .filter(p -> name == null || name.isEmpty() || String.valueOf(p.get("name")).contains(name))
                .filter(p -> auditStatus == null || Objects.equals(((Number) p.get("auditStatus")).intValue(), auditStatus))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> saveMerchantProduct(Long userId, Map<String, Object> payload) {
        Long merchantId = getMerchantIdByUserId(userId);
        Long id = toLong(payload.get("id"));
        Map<String, Object> row;
        if (id == null) {
            row = new LinkedHashMap<>();
            row.put("id", productIdSeq.incrementAndGet());
            row.put("merchantId", merchantId);
            row.put("sales", 0);
            row.put("auditStatus", 1);
            products.add(0, row);
        } else {
            row = products.stream()
                    .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), id))
                    .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                    .findFirst().orElse(null);
            if (row == null) {
                return null;
            }
        }
        row.put("name", String.valueOf(payload.getOrDefault("name", "新商品")));
        row.put("description", String.valueOf(payload.getOrDefault("description", "")));
        row.put("categoryId", toLong(payload.getOrDefault("categoryId", 1L)));
        Long cid = ((Number) row.get("categoryId")).longValue();
        row.put("categoryName", cid == 1 ? "水果" : cid == 2 ? "蔬菜" : "有机食品");
        row.put("price", BigDecimal.valueOf(toDouble(payload.getOrDefault("price", 1))));
        row.put("originalPrice", BigDecimal.valueOf(toDouble(payload.getOrDefault("originalPrice", row.get("price")))));
        row.put("stock", toInt(payload.getOrDefault("stock", 100)));
        row.put("unit", String.valueOf(payload.getOrDefault("unit", "份")));
        String imageUrl = String.valueOf(payload.getOrDefault("mainImage", image("product-merchant-" + row.get("id"))));
        row.put("mainImage", imageUrl);
        row.put("images", Arrays.asList(imageUrl, image("product-merchant-detail-" + row.get("id"))));
        row.put("updateTime", now());

        traceByProduct.computeIfAbsent(((Number) row.get("id")).longValue(), pid -> trace(pid, String.valueOf(row.get("name"))));
        return new LinkedHashMap<>(row);
    }

    public boolean updateMerchantProductStatus(Long userId, Long id, Integer auditStatus) {
        Long merchantId = getMerchantIdByUserId(userId);
        Map<String, Object> row = products.stream()
                .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), id))
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .findFirst().orElse(null);
        if (row == null) {
            return false;
        }
        row.put("auditStatus", auditStatus == null ? 1 : auditStatus);
        return true;
    }

    public Map<String, Object> listMerchantTraces(Long userId, Integer page, Integer pageSize) {
        Long merchantId = getMerchantIdByUserId(userId);
        List<Map<String, Object>> rows = products.stream()
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .map(p -> {
                    Long pid = ((Number) p.get("id")).longValue();
                    Map<String, Object> trace = new LinkedHashMap<>(traceByProduct.getOrDefault(pid, trace(pid, String.valueOf(p.get("name")))));
                    trace.put("productId", pid);
                    trace.put("productName", p.get("name"));
                    return trace;
                })
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> saveMerchantTrace(Long userId, Long productId, Map<String, Object> payload) {
        Long merchantId = getMerchantIdByUserId(userId);
        Map<String, Object> product = products.stream()
                .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), productId))
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .findFirst().orElse(null);
        if (product == null) {
            return null;
        }
        Map<String, Object> trace = traceByProduct.computeIfAbsent(productId, pid -> trace(pid, String.valueOf(product.get("name"))));
        if (payload.containsKey("originName")) trace.put("originName", payload.get("originName"));
        if (payload.containsKey("originAddress")) trace.put("originAddress", payload.get("originAddress"));
        if (payload.containsKey("plantMethod")) trace.put("plantMethod", payload.get("plantMethod"));
        if (payload.containsKey("plantTime")) trace.put("plantTime", payload.get("plantTime"));
        if (payload.containsKey("harvestTime")) trace.put("harvestTime", payload.get("harvestTime"));
        if (payload.containsKey("storageCondition")) trace.put("storageCondition", payload.get("storageCondition"));
        if (payload.containsKey("shelfLife")) trace.put("shelfLife", toInt(payload.get("shelfLife")));
        if (payload.containsKey("testReport")) trace.put("testReport", payload.get("testReport"));
        trace.put("productName", product.get("name"));
        return new LinkedHashMap<>(trace);
    }

    public Map<String, Object> generateTraceQr(Long productId) {
        Map<String, Object> trace = traceByProduct.get(productId);
        if (trace == null) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        String traceUrl = "http://localhost:3000/products/" + productId + "/trace";
        data.put("productId", productId);
        data.put("traceUrl", traceUrl);
        data.put("qrUrl", "https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=" + traceUrl);
        return data;
    }

    public Map<String, Object> listUserOrders(Integer page, Integer pageSize, String status, String orderNumber) {
        List<Map<String, Object>> filtered = userOrders.stream()
                .filter(o -> status == null || status.isEmpty() || Objects.equals(o.get("status"), status))
                .filter(o -> orderNumber == null || orderNumber.isEmpty() || String.valueOf(o.get("orderNumber")).contains(orderNumber))
                .collect(Collectors.toList());

        int p = page == null || page < 1 ? 1 : page;
        int s = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int from = Math.min((p - 1) * s, filtered.size());
        int to = Math.min(from + s, filtered.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", filtered.subList(from, to));
        result.put("total", filtered.size());
        result.put("current", p);
        result.put("size", s);
        return result;
    }

    public Map<String, Object> getUserOrderDetail(Long id) {
        return userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.get("id")).longValue(), id))
                .findFirst().map(LinkedHashMap::new).orElse(null);
    }

    public Map<String, Object> createOrder(Long userId, Map<String, Object> payload) {
        long id = orderIdSeq.incrementAndGet();
        Map<String, Object> order = sampleOrder(id, "pending", 0);
        order.put("id", id);
        order.put("orderNumber", "FV" + System.currentTimeMillis());
        order.put("createTime", now());
        order.put("paymentTime", null);
        order.put("deliveryTime", null);
        order.put("receiveTime", null);
        if (payload != null && payload.get("paymentMethod") != null) {
            order.put("paymentMethod", payload.get("paymentMethod"));
        }
        if (payload != null && payload.get("receiverName") != null) {
            order.put("receiverName", payload.get("receiverName"));
            order.put("receiverPhone", payload.get("receiverPhone"));
            order.put("address", payload.get("receiverAddress"));
        }
        if (payload != null && payload.get("items") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            if (!items.isEmpty()) {
                order.put("products", items);
            }
        }
        if (payload != null && payload.get("totalAmount") instanceof Number) {
            order.put("totalAmount", ((Number) payload.get("totalAmount")).doubleValue());
        }
        order.put("userId", userId);
        order.put("merchantId", 1L);
        order.put("logisticsCompany", "待分配");
        order.put("logisticsNo", "-");
        order.put("logisticsTracks", new ArrayList<>(Collections.singletonList(logisticsTrack("订单创建", "用户已提交订单，等待支付"))));
        order.put("reviewed", false);
        userOrders.add(0, order);
        return new LinkedHashMap<>(order);
    }

    public boolean updateOrderStatus(Long id, String status, Integer adminStatus) {
        Map<String, Object> found = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (found == null) {
            return false;
        }
        found.put("status", status);
        found.put("adminStatus", adminStatus);
        if ("completed".equals(status)) {
            found.put("receiveTime", now());
            appendLogisticsTrack(found, "订单完成", "用户已确认收货，订单完成");
        }
        if ("delivered".equals(status)) {
            found.put("deliveryTime", now());
            appendLogisticsTrack(found, "运输中", "包裹已出库，正在配送");
        }
        if (!"pending".equals(status) && found.get("paymentTime") == null) {
            found.put("paymentTime", now());
            found.put("logisticsCompany", "郑州冷链快配");
            found.put("logisticsNo", "ZZL" + System.currentTimeMillis());
            appendLogisticsTrack(found, "支付成功", "订单支付完成，进入备货流程");
        }
        if ("shipped".equals(status)) {
            appendLogisticsTrack(found, "待发货", "商家已接单，正在分拣打包");
        }
        if ("cancelled".equals(status)) {
            appendLogisticsTrack(found, "订单取消", "订单已取消或进入退款流程");
        }
        return true;
    }

    public Map<String, Object> listAdminProducts(Integer page, Integer pageSize, String name, Long categoryId, Integer auditStatus) {
        List<Map<String, Object>> filtered = products.stream()
                .filter(p -> name == null || name.isEmpty() || String.valueOf(p.get("name")).contains(name))
                .filter(p -> categoryId == null || Objects.equals(((Number) p.get("categoryId")).longValue(), categoryId))
                .filter(p -> auditStatus == null || Objects.equals(((Number) p.get("auditStatus")).intValue(), auditStatus))
                .map(p -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", p.get("id"));
                    row.put("name", p.get("name"));
                    row.put("coverImage", p.get("mainImage"));
                    row.put("images", String.join(",", castListString(p.get("images"))));
                    row.put("categoryName", p.get("categoryName"));
                    row.put("price", p.get("price"));
                    row.put("stock", p.get("stock"));
                    row.put("sales", p.get("sales"));
                    row.put("auditStatus", p.get("auditStatus"));
                    row.put("createTime", now());
                    return row;
                }).collect(Collectors.toList());

        return pageResult(filtered, page, pageSize);
    }

    public Map<String, Object> getAdminProductDetail(Long id) {
        Map<String, Object> product = products.stream()
                .filter(item -> Objects.equals(((Number) item.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (product == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>(product);
        row.put("merchantName", getMerchantNameById(((Number) product.getOrDefault("merchantId", 1L)).longValue()));
        row.put("trace", traceByProduct.get(id));
        return row;
    }

    public boolean auditProduct(Long id, Integer status) {
        Map<String, Object> p = products.stream().filter(item -> Objects.equals(((Number) item.get("id")).longValue(), id)).findFirst().orElse(null);
        if (p == null) return false;
        p.put("auditStatus", status);
        return true;
    }

    public Map<String, Object> listAdminOrders(Integer page, Integer pageSize, String orderNo, String phone, Integer status) {
        List<Map<String, Object>> rows = userOrders.stream()
                .map(this::toAdminOrder)
                .filter(o -> orderNo == null || orderNo.isEmpty() || String.valueOf(o.get("orderNo")).contains(orderNo))
                .filter(o -> phone == null || phone.isEmpty() || String.valueOf(o.get("phone")).contains(phone))
                .filter(o -> status == null || Objects.equals(((Number) o.get("status")).intValue(), status))
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> getAdminOrderDetail(Long id) {
        Map<String, Object> row = userOrders.stream()
                .filter(order -> Objects.equals(((Number) order.get("id")).longValue(), id))
                .findFirst().orElse(null);
        return row == null ? null : new LinkedHashMap<>(row);
    }

    public boolean shipOrder(Long id) {
        return updateOrderStatus(id, "delivered", 2);
    }

    public boolean shipOrder(Long id, String logisticsCompany, String logisticsNo) {
        Map<String, Object> found = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (found == null) {
            return false;
        }
        found.put("status", "delivered");
        found.put("adminStatus", 2);
        found.put("deliveryTime", now());
        found.put("logisticsCompany", logisticsCompany == null || logisticsCompany.isEmpty() ? "郑州冷链快配" : logisticsCompany);
        found.put("logisticsNo", logisticsNo == null || logisticsNo.isEmpty() ? "ZZL" + System.currentTimeMillis() : logisticsNo);
        appendLogisticsTrack(found, "已发货", "商家已发货，包裹进入冷链配送");
        appendLogisticsTrack(found, "运输中", "配送中，请保持电话畅通");
        return true;
    }

    public Map<String, Object> listMerchantOrders(Long userId, Integer page, Integer pageSize, String status, String orderNo) {
        Long merchantId = getMerchantIdByUserId(userId);
        List<Map<String, Object>> rows = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .filter(o -> status == null || status.isEmpty() || Objects.equals(String.valueOf(o.get("status")), status))
                .filter(o -> orderNo == null || orderNo.isEmpty() || String.valueOf(o.get("orderNumber")).contains(orderNo))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> applyRefund(Long orderId, Long userId, Map<String, Object> payload) {
        Map<String, Object> order = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.get("id")).longValue(), orderId))
                .findFirst().orElse(null);
        if (order == null) {
            return null;
        }
        order.put("status", "refunding");
        order.put("adminStatus", 6);
        appendLogisticsTrack(order, "售后申请", "用户发起售后申请，等待平台处理");

        Map<String, Object> complaint = new LinkedHashMap<>();
        long cid = complaintIdSeq.incrementAndGet();
        complaint.put("id", cid);
        complaint.put("orderId", orderId);
        complaint.put("userId", userId);
        complaint.put("merchantId", order.getOrDefault("merchantId", 1L));
        complaint.put("orderNo", order.get("orderNumber"));
        complaint.put("type", payload == null ? "退款申请" : String.valueOf(payload.getOrDefault("type", "退款申请")));
        complaint.put("reason", payload == null ? "用户申请售后" : String.valueOf(payload.getOrDefault("reason", "用户申请售后")));
        complaint.put("images", payload == null ? Collections.emptyList() : castListString(payload.get("images")));
        complaint.put("status", "pending");
        complaint.put("statusText", "待处理");
        complaint.put("createTime", now());
        complaint.put("handleTime", null);
        complaint.put("handleResult", null);
        complaints.add(0, complaint);
        order.put("afterSaleId", cid);
        return new LinkedHashMap<>(complaint);
    }

    public Map<String, Object> listComplaints(Integer page, Integer pageSize, String status, String orderNo) {
        List<Map<String, Object>> rows = complaints.stream()
                .filter(c -> status == null || status.isEmpty() || Objects.equals(String.valueOf(c.get("status")), status))
                .filter(c -> orderNo == null || orderNo.isEmpty() || String.valueOf(c.get("orderNo")).contains(orderNo))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public boolean handleComplaint(Long id, String result, String remark) {
        Map<String, Object> complaint = complaints.stream()
                .filter(c -> Objects.equals(((Number) c.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (complaint == null) {
            return false;
        }
        complaint.put("status", "handled");
        complaint.put("statusText", "已处理");
        complaint.put("handleResult", (result == null || result.isEmpty()) ? "同意退款" : result);
        complaint.put("handleRemark", remark == null ? "" : remark);
        complaint.put("handleTime", now());

        Long orderId = ((Number) complaint.get("orderId")).longValue();
        Map<String, Object> order = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.get("id")).longValue(), orderId))
                .findFirst().orElse(null);
        if (order != null) {
            String handleResult = String.valueOf(complaint.get("handleResult"));
            if (handleResult.contains("拒绝")) {
                order.put("status", "delivered");
                order.put("adminStatus", 2);
                appendLogisticsTrack(order, "售后驳回", "平台已驳回售后申请");
            } else {
                order.put("status", "cancelled");
                order.put("adminStatus", 5);
                appendLogisticsTrack(order, "退款完成", "平台已处理退款，订单关闭");
            }
        }
        return true;
    }

    public Map<String, Object> listMerchants(Integer page, Integer pageSize, String name, String contactName, String contactPhone, Integer auditStatus) {
        List<Map<String, Object>> rows = merchants.stream()
                .filter(m -> name == null || name.isEmpty() || String.valueOf(m.get("name")).contains(name))
                .filter(m -> contactName == null || contactName.isEmpty() || String.valueOf(m.get("contactName")).contains(contactName))
                .filter(m -> contactPhone == null || contactPhone.isEmpty() || String.valueOf(m.get("contactPhone")).contains(contactPhone))
                .filter(m -> auditStatus == null || Objects.equals(((Number) m.get("auditStatus")).intValue(), auditStatus))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> getMerchantDetail(Long id) {
        Map<String, Object> merchant = merchants.stream()
                .filter(item -> Objects.equals(((Number) item.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (merchant == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>(merchant);
        Long merchantId = ((Number) merchant.get("id")).longValue();
        int productCount = (int) products.stream()
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .count();
        int orderCount = (int) userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .count();
        row.put("productCount", productCount);
        row.put("orderCount", orderCount);
        return row;
    }

    public Map<String, Object> listAdminTraceTemplates() {
        List<Map<String, Object>> rows = traceTemplates.stream()
                .sorted((a, b) -> Long.compare(((Number) b.get("id")).longValue(), ((Number) a.get("id")).longValue()))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", rows);
        result.put("total", rows.size());
        return result;
    }

    public Map<String, Object> saveTraceTemplate(Map<String, Object> payload) {
        Long id = toLong(payload.get("id"));
        Map<String, Object> row;
        if (id == null) {
            row = new LinkedHashMap<>();
            row.put("id", traceTemplateIdSeq.incrementAndGet());
            traceTemplates.add(0, row);
        } else {
            row = traceTemplates.stream()
                    .filter(item -> Objects.equals(((Number) item.get("id")).longValue(), id))
                    .findFirst().orElse(null);
            if (row == null) {
                row = new LinkedHashMap<>();
                row.put("id", id);
                traceTemplates.add(0, row);
            }
        }
        row.put("name", String.valueOf(payload.getOrDefault("name", "溯源模板")));
        row.put("description", String.valueOf(payload.getOrDefault("description", "")));
        row.put("fields", castListString(payload.get("fields")));
        row.put("updateTime", now());
        return new LinkedHashMap<>(row);
    }

    public boolean deleteTraceTemplate(Long id) {
        return traceTemplates.removeIf(item -> Objects.equals(((Number) item.get("id")).longValue(), id));
    }

    public Map<String, Object> listAdminTraces(Integer page, Integer pageSize, Integer auditStatus, String keyword) {
        List<Map<String, Object>> rows = products.stream()
                .map(product -> {
                    Long productId = ((Number) product.get("id")).longValue();
                    Map<String, Object> trace = traceByProduct.getOrDefault(productId, trace(productId, String.valueOf(product.get("name"))));
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("productId", productId);
                    row.put("productName", product.get("name"));
                    row.put("merchantName", getMerchantNameById(((Number) product.getOrDefault("merchantId", 1L)).longValue()));
                    row.put("originName", trace.get("originName"));
                    row.put("plantTime", trace.get("plantTime"));
                    row.put("harvestTime", trace.get("harvestTime"));
                    row.put("auditStatus", trace.getOrDefault("auditStatus", 1));
                    row.put("auditRemark", trace.getOrDefault("auditRemark", ""));
                    row.put("auditTime", trace.getOrDefault("auditTime", null));
                    return row;
                })
                .filter(row -> keyword == null || keyword.isEmpty()
                        || String.valueOf(row.get("productName")).contains(keyword)
                        || String.valueOf(row.get("merchantName")).contains(keyword))
                .filter(row -> auditStatus == null || Objects.equals(((Number) row.get("auditStatus")).intValue(), auditStatus))
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public boolean auditTrace(Long productId, Integer status, String remark) {
        Map<String, Object> trace = traceByProduct.get(productId);
        if (trace == null) {
            return false;
        }
        trace.put("auditStatus", status == null ? 1 : status);
        trace.put("auditRemark", remark == null ? "" : remark);
        trace.put("auditTime", now());
        return true;
    }

    public Map<String, Object> getTraceStats() {
        int totalProduct = products.size();
        long tracedCount = products.stream()
                .filter(product -> traceByProduct.containsKey(((Number) product.get("id")).longValue()))
                .count();
        long pendingCount = traceByProduct.values().stream()
                .filter(trace -> Objects.equals(toInt(trace.getOrDefault("auditStatus", 1)), 0))
                .count();
        long approvedCount = traceByProduct.values().stream()
                .filter(trace -> Objects.equals(toInt(trace.getOrDefault("auditStatus", 1)), 1))
                .count();
        long rejectedCount = traceByProduct.values().stream()
                .filter(trace -> Objects.equals(toInt(trace.getOrDefault("auditStatus", 1)), 2))
                .count();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProductCount", totalProduct);
        stats.put("traceCoverageCount", tracedCount);
        stats.put("traceCoverageRate", totalProduct == 0 ? 0D : (double) tracedCount / totalProduct);
        stats.put("pendingAuditCount", pendingCount);
        stats.put("approvedCount", approvedCount);
        stats.put("rejectedCount", rejectedCount);
        return stats;
    }

    public boolean auditMerchant(Long id, Integer status) {
        Map<String, Object> m = merchants.stream().filter(item -> Objects.equals(((Number) item.get("id")).longValue(), id)).findFirst().orElse(null);
        if (m == null) return false;
        m.put("auditStatus", status);
        return true;
    }

    public Map<String, Object> getStats(int userCount) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("userCount", userCount);
        stats.put("productCount", products.size());
        stats.put("orderCount", userOrders.size());
        double total = userOrders.stream().mapToDouble(o -> ((Number) o.get("totalAmount")).doubleValue()).sum();
        stats.put("totalAmount", (long) (total * 100));
        long pending = complaints.stream().filter(c -> "pending".equals(String.valueOf(c.get("status")))).count();
        long handled = complaints.stream().filter(c -> "handled".equals(String.valueOf(c.get("status")))).count();
        stats.put("complaintPendingCount", pending);
        stats.put("complaintHandledCount", handled);
        return stats;
    }

    public Map<String, Object> listUserAfterSales(Long userId, Integer page, Integer pageSize) {
        List<Map<String, Object>> rows = complaints.stream()
                .filter(c -> Objects.equals(((Number) c.getOrDefault("userId", 0L)).longValue(), userId))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, pageSize);
    }

    public Map<String, Object> submitOrderReview(Long userId, Long orderId, Map<String, Object> payload) {
        Map<String, Object> order = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.get("id")).longValue(), orderId))
                .filter(o -> Objects.equals(((Number) o.getOrDefault("userId", 0L)).longValue(), userId))
                .findFirst().orElse(null);
        if (order == null) {
            return null;
        }
        if (!"completed".equals(String.valueOf(order.get("status")))) {
            return Collections.emptyMap();
        }

        Map<String, Object> review = orderReviews.stream()
                .filter(r -> Objects.equals(((Number) r.get("orderId")).longValue(), orderId))
                .filter(r -> Objects.equals(((Number) r.get("userId")).longValue(), userId))
                .findFirst().orElse(null);
        if (review == null) {
            review = new LinkedHashMap<>();
            review.put("id", orderReviews.size() + 1L);
            review.put("orderId", orderId);
            review.put("userId", userId);
            orderReviews.add(0, review);
        }

        int rating = toInt(payload == null ? null : payload.get("rating"));
        if (rating < 1 || rating > 5) {
            rating = 5;
        }
        review.put("rating", rating);
        review.put("content", payload == null ? "" : String.valueOf(payload.getOrDefault("content", "")));
        review.put("anonymous", payload != null && Boolean.TRUE.equals(payload.get("anonymous")));
        review.put("images", payload == null ? Collections.emptyList() : castListString(payload.get("images")));
        review.put("createTime", now());

        List<Map<String, Object>> products = payload == null ? Collections.emptyList() : castListMap(payload.get("products"));
        review.put("products", products);

        order.put("reviewed", true);
        order.put("reviewTime", now());
        return new LinkedHashMap<>(review);
    }

    public Map<String, Object> getOrderReview(Long userId, Long orderId) {
        Map<String, Object> review = orderReviews.stream()
                .filter(r -> Objects.equals(((Number) r.get("orderId")).longValue(), orderId))
                .filter(r -> Objects.equals(((Number) r.get("userId")).longValue(), userId))
                .findFirst().orElse(null);
        return review == null ? null : new LinkedHashMap<>(review);
    }

    public Map<String, Object> applyMerchant(Long userId, Map<String, Object> payload) {
        Map<String, Object> item = new LinkedHashMap<>();
        long id = merchantIdSeq.incrementAndGet();
        item.put("id", id);
        item.put("name", payload.getOrDefault("shopName", "新商家"));
        item.put("shopName", payload.getOrDefault("shopName", "新商家"));
        item.put("businessType", payload.getOrDefault("businessType", "GENERAL_STORE"));
        item.put("shopDescription", payload.getOrDefault("shopDescription", ""));
        item.put("contactName", payload.getOrDefault("contactName", ""));
        item.put("contactPhone", payload.getOrDefault("contactPhone", ""));
        item.put("address", payload.getOrDefault("address", ""));
        item.put("logoUrl", image("merchant"));
        item.put("auditStatus", 0);
        item.put("createTime", now());

        merchants.add(0, item);
        merchantByUserId.put(userId, item);
        return new LinkedHashMap<>(item);
    }

    public Map<String, Object> getMerchantInfo(Long userId) {
        Map<String, Object> m = merchantByUserId.get(userId);
        if (m != null) {
            return new LinkedHashMap<>(m);
        }
        return new LinkedHashMap<>(merchants.get(0));
    }

    public Map<String, Object> updateMerchantInfo(Long userId, Map<String, Object> payload) {
        Map<String, Object> m = merchantByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>(merchants.get(0)));
        if (payload != null) {
            m.putAll(payload);
        }
        m.put("name", m.getOrDefault("shopName", m.get("name")));
        m.put("updateTime", now());
        return new LinkedHashMap<>(m);
    }

    public Map<String, Object> getTraceDetail(Long productId) {
        Map<String, Object> trace = traceByProduct.get(productId);
        return trace == null ? null : new LinkedHashMap<>(trace);
    }

    public Map<String, Object> listCirclePosts(Long userId, Integer page, Integer size, Boolean followedOnly) {
        Set<Long> followed = userId == null
                ? Collections.emptySet()
                : followMerchantIdsByUser.getOrDefault(userId, Collections.emptySet());
        boolean onlyFollowed = Boolean.TRUE.equals(followedOnly);
        List<Map<String, Object>> rows = circlePosts.stream()
                .filter(post -> !onlyFollowed || followed.contains(((Number) post.get("merchantId")).longValue()))
                .sorted((a, b) -> Long.compare(((Number) b.get("id")).longValue(), ((Number) a.get("id")).longValue()))
                .map(post -> {
                    Map<String, Object> row = new LinkedHashMap<>(post);
                    Long merchantId = ((Number) post.get("merchantId")).longValue();
                    row.put("isFollowed", followed.contains(merchantId));
                    return row;
                })
                .collect(Collectors.toList());
        return pageResult(rows, page, size);
    }

    public Map<String, Object> getCirclePost(Long id) {
        Map<String, Object> post = circlePosts.stream()
                .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), id))
                .findFirst().orElse(null);
        return post == null ? null : new LinkedHashMap<>(post);
    }

    public boolean followMerchant(Long userId, Long merchantId) {
        boolean exists = merchants.stream().anyMatch(m -> Objects.equals(((Number) m.get("id")).longValue(), merchantId));
        if (!exists) {
            return false;
        }
        followMerchantIdsByUser.computeIfAbsent(userId, k -> new LinkedHashSet<>()).add(merchantId);
        return true;
    }

    public boolean unfollowMerchant(Long userId, Long merchantId) {
        Set<Long> ids = followMerchantIdsByUser.get(userId);
        return ids != null && ids.remove(merchantId);
    }

    public List<Map<String, Object>> listFollowedMerchants(Long userId) {
        Set<Long> ids = followMerchantIdsByUser.getOrDefault(userId, Collections.emptySet());
        return merchants.stream()
                .filter(m -> ids.contains(((Number) m.get("id")).longValue()))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
    }

    public boolean toggleCircleLike(Long id) {
        Map<String, Object> post = circlePosts.stream()
                .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (post == null) {
            return false;
        }
        Integer likeCount = ((Number) post.getOrDefault("likeCount", 0)).intValue();
        post.put("likeCount", likeCount + 1);
        return true;
    }

    public Map<String, Object> addCircleComment(Long id, Long userId, String content) {
        Map<String, Object> post = circlePosts.stream()
                .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), id))
                .findFirst().orElse(null);
        if (post == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> comments = (List<Map<String, Object>>) post.computeIfAbsent("comments", k -> new ArrayList<>());
        Map<String, Object> comment = new LinkedHashMap<>();
        comment.put("id", comments.size() + 1L);
        comment.put("userId", userId);
        comment.put("nickname", "用户" + userId);
        comment.put("content", content);
        comment.put("createTime", now());
        comments.add(comment);
        post.put("commentCount", comments.size());
        return comment;
    }

    public Map<String, Object> listMerchantCirclePosts(Long userId, Integer page, Integer size) {
        Long merchantId = getMerchantIdByUserId(userId);
        List<Map<String, Object>> rows = circlePosts.stream()
                .filter(p -> Objects.equals(((Number) p.get("merchantId")).longValue(), merchantId))
                .sorted((a, b) -> Long.compare(((Number) b.get("id")).longValue(), ((Number) a.get("id")).longValue()))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
        return pageResult(rows, page, size);
    }

    public Map<String, Object> createMerchantCirclePost(Long userId, Map<String, Object> payload) {
        Long merchantId = getMerchantIdByUserId(userId);
        String merchantName = getMerchantNameByUserId(userId);
        long id = circlePostIdSeq.incrementAndGet();
        Map<String, Object> post = new LinkedHashMap<>();
        post.put("id", id);
        post.put("merchantId", merchantId);
        post.put("merchantName", merchantName);
        post.put("title", String.valueOf(payload.getOrDefault("title", "商家动态")));
        post.put("content", String.valueOf(payload.getOrDefault("content", "")));
        post.put("images", castListString(payload.get("images")).isEmpty()
                ? Collections.singletonList(image("circle-new-" + id))
                : castListString(payload.get("images")));
        post.put("productIds", castListLong(payload.get("productIds")));
        post.put("viewCount", 0);
        post.put("likeCount", 0);
        post.put("commentCount", 0);
        post.put("createTime", now());
        post.put("comments", new ArrayList<>());
        circlePosts.add(0, post);
        return new LinkedHashMap<>(post);
    }

    public Map<String, Object> updateMerchantCirclePost(Long userId, Long id, Map<String, Object> payload) {
        Long merchantId = getMerchantIdByUserId(userId);
        Map<String, Object> post = circlePosts.stream()
                .filter(p -> Objects.equals(((Number) p.get("id")).longValue(), id))
                .filter(p -> Objects.equals(((Number) p.get("merchantId")).longValue(), merchantId))
                .findFirst().orElse(null);
        if (post == null) {
            return null;
        }
        if (payload.containsKey("title")) {
            post.put("title", String.valueOf(payload.get("title")));
        }
        if (payload.containsKey("content")) {
            post.put("content", String.valueOf(payload.get("content")));
        }
        if (payload.containsKey("images")) {
            List<String> images = castListString(payload.get("images"));
            post.put("images", images.isEmpty() ? post.get("images") : images);
        }
        if (payload.containsKey("productIds")) {
            post.put("productIds", castListLong(payload.get("productIds")));
        }
        post.put("updateTime", now());
        return new LinkedHashMap<>(post);
    }

    public boolean deleteMerchantCirclePost(Long userId, Long id) {
        Long merchantId = getMerchantIdByUserId(userId);
        return circlePosts.removeIf(p ->
                Objects.equals(((Number) p.get("id")).longValue(), id) &&
                Objects.equals(((Number) p.get("merchantId")).longValue(), merchantId)
        );
    }

    public Map<String, Object> askAi(Long userId, String question) {
        return saveAiConversation(userId, null, question, buildLocalAiAnswer(question));
    }

    public Map<String, Object> saveAiConversation(Long userId, String question, String answer) {
        return saveAiConversation(userId, null, question, answer);
    }

    public Map<String, Object> saveAiConversation(Long userId, Long sessionId, String question, String answer) {
        Long sid = resolveSessionId(userId, sessionId);
        Map<String, Object> qa = new LinkedHashMap<>();
        qa.put("id", aiMessageIdSeq.incrementAndGet());
        qa.put("sessionId", sid);
        qa.put("question", question);
        qa.put("answer", answer);
        qa.put("createTime", now());
        aiHistoryByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(qa);
        return qa;
    }

    private String buildLocalAiAnswer(String question) {
        String answer;
        if (question.contains("保存") || question.contains("保鲜")) {
            answer = "建议冷藏 4-8℃，叶菜类建议 2 天内食用，水果建议分区存放并避免挤压。";
        } else if (question.contains("营养") || question.contains("功效")) {
            answer = "果蔬富含膳食纤维和维生素，建议每天摄入 500g 以上并搭配多种颜色。";
        } else if (question.contains("挑选") || question.contains("怎么选")) {
            answer = "优先看外观完整度和气味，新鲜蔬菜叶片挺拔、无发黄，水果有自然果香。";
        } else {
            answer = "建议优先选择当季本地果蔬，口感和营养更好。你可以告诉我具体品类，我给你更细建议。";
        }
        return answer;
    }

    public List<Map<String, Object>> getAiHistory(Long userId) {
        return getAiHistoryBySession(userId, getActiveAiSessionId(userId));
    }

    public List<Map<String, Object>> getAiHistoryBySession(Long userId, Long sessionId) {
        Long sid = resolveSessionId(userId, sessionId);
        return aiHistoryByUser.getOrDefault(userId, Collections.emptyList()).stream()
                .filter(item -> Objects.equals(toLong(item.get("sessionId")), sid))
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listAiSessions(Long userId) {
        Long activeId = getActiveAiSessionId(userId);
        Map<Long, List<Map<String, Object>>> grouped = aiHistoryByUser.getOrDefault(userId, Collections.emptyList())
                .stream()
                .collect(Collectors.groupingBy(item -> toLong(item.get("sessionId")), LinkedHashMap::new, Collectors.toList()));
        if (!grouped.containsKey(activeId)) {
            grouped.put(activeId, new ArrayList<>());
        }
        List<Map<String, Object>> sessions = grouped.entrySet().stream().map(entry -> {
            Long sid = entry.getKey();
            List<Map<String, Object>> rows = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", sid);
            row.put("count", rows.size());
            row.put("active", Objects.equals(sid, activeId));
            if (rows.isEmpty()) {
                row.put("title", "新对话");
                row.put("lastTime", now());
            } else {
                Map<String, Object> last = rows.get(rows.size() - 1);
                String lastQuestion = String.valueOf(last.getOrDefault("question", "新对话")).trim();
                row.put("title", lastQuestion.length() > 20 ? lastQuestion.substring(0, 20) + "..." : lastQuestion);
                row.put("lastTime", String.valueOf(last.getOrDefault("createTime", now())));
            }
            return row;
        }).sorted((a, b) -> String.valueOf(b.get("lastTime")).compareTo(String.valueOf(a.get("lastTime"))))
                .collect(Collectors.toList());
        return sessions;
    }

    public Map<String, Object> createAiSession(Long userId) {
        long id = aiSessionIdSeq.incrementAndGet();
        aiActiveSessionByUser.put(userId, id);
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("id", id);
        session.put("title", "新对话");
        session.put("count", 0);
        session.put("active", true);
        session.put("lastTime", now());
        return session;
    }

    public boolean switchAiSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            return false;
        }
        boolean exists = listAiSessions(userId).stream()
                .anyMatch(session -> Objects.equals(toLong(session.get("id")), sessionId));
        if (!exists) {
            return false;
        }
        aiActiveSessionByUser.put(userId, sessionId);
        return true;
    }

    public boolean deleteAiSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            return false;
        }
        List<Map<String, Object>> all = aiHistoryByUser.getOrDefault(userId, new ArrayList<>());
        boolean removed = all.removeIf(item -> Objects.equals(toLong(item.get("sessionId")), sessionId));
        // 删除的是空会话也算成功（如果它是当前会话）
        if (Objects.equals(aiActiveSessionByUser.get(userId), sessionId)) {
            Long next = aiHistoryByUser.getOrDefault(userId, Collections.emptyList()).stream()
                    .map(item -> toLong(item.get("sessionId")))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(aiSessionIdSeq.incrementAndGet());
            aiActiveSessionByUser.put(userId, next);
            removed = true;
        }
        return removed;
    }

    public Long getActiveAiSessionId(Long userId) {
        return aiActiveSessionByUser.computeIfAbsent(userId, k -> aiSessionIdSeq.incrementAndGet());
    }

    private Long resolveSessionId(Long userId, Long sessionId) {
        Long sid = sessionId == null ? getActiveAiSessionId(userId) : sessionId;
        aiActiveSessionByUser.put(userId, sid);
        return sid;
    }

    public Map<String, Object> getMerchantStats(Long userId) {
        Long merchantId = getMerchantIdByUserId(userId);
        List<Map<String, Object>> mProducts = products.stream()
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .collect(Collectors.toList());
        List<Map<String, Object>> mOrders = userOrders.stream()
                .filter(o -> Objects.equals(((Number) o.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .collect(Collectors.toList());
        List<Map<String, Object>> mPosts = circlePosts.stream()
                .filter(p -> Objects.equals(((Number) p.getOrDefault("merchantId", 1L)).longValue(), merchantId))
                .collect(Collectors.toList());

        double totalSalesAmount = mOrders.stream()
                .filter(o -> "completed".equals(String.valueOf(o.get("status"))))
                .mapToDouble(o -> ((Number) o.getOrDefault("totalAmount", 0D)).doubleValue())
                .sum();
        int orderCount = mOrders.size();
        int completedOrderCount = (int) mOrders.stream().filter(o -> "completed".equals(String.valueOf(o.get("status")))).count();
        int productCount = mProducts.size();
        int visitorCount = mPosts.stream().mapToInt(p -> ((Number) p.getOrDefault("viewCount", 0)).intValue()).sum();

        List<Map<String, Object>> topProducts = mProducts.stream()
                .sorted((a, b) -> Integer.compare(((Number) b.getOrDefault("sales", 0)).intValue(), ((Number) a.getOrDefault("sales", 0)).intValue()))
                .limit(5)
                .map(p -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", p.get("id"));
                    row.put("name", p.get("name"));
                    row.put("sales", p.getOrDefault("sales", 0));
                    row.put("stock", p.getOrDefault("stock", 0));
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSalesAmount", totalSalesAmount);
        stats.put("orderCount", orderCount);
        stats.put("completedOrderCount", completedOrderCount);
        stats.put("avgOrderAmount", completedOrderCount == 0 ? 0D : totalSalesAmount / completedOrderCount);
        stats.put("productCount", productCount);
        stats.put("visitorCount", visitorCount);
        stats.put("topProducts", topProducts);
        return stats;
    }

    public List<String> getAiFaq() {
        return Arrays.asList(
                "西红柿怎么保存更久？",
                "黄瓜怎么挑选更新鲜？",
                "车厘子适合冷藏还是常温？",
                "每天吃多少果蔬更科学？"
        );
    }

    private Map<String, Object> pageResult(List<Map<String, Object>> rows, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int s = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int from = Math.min((p - 1) * s, rows.size());
        int to = Math.min(from + s, rows.size());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", rows.subList(from, to));
        result.put("total", rows.size());
        result.put("current", p);
        result.put("size", s);
        return result;
    }

    private Map<String, Object> toAdminOrder(Map<String, Object> userOrder) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("id", userOrder.get("id"));
        o.put("orderNo", userOrder.get("orderNumber"));
        o.put("phone", userOrder.get("receiverPhone"));
        o.put("totalAmount", userOrder.get("totalAmount"));
        o.put("status", userOrder.get("adminStatus"));
        o.put("orderStatus", userOrder.get("status"));
        o.put("logisticsCompany", userOrder.get("logisticsCompany"));
        o.put("logisticsNo", userOrder.get("logisticsNo"));
        o.put("payTime", userOrder.get("paymentTime"));
        o.put("createTime", userOrder.get("createTime"));
        return o;
    }

    private Map<String, Object> toHomeProduct(Map<String, Object> p) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", p.get("id"));
        item.put("name", p.get("name"));
        item.put("description", p.get("description"));
        item.put("merchantName", getMerchantNameById(((Number) p.getOrDefault("merchantId", 1L)).longValue()));
        item.put("price", p.get("price"));
        item.put("originalPrice", p.get("originalPrice"));
        item.put("image", p.get("mainImage"));
        return item;
    }

    private Map<String, Object> category(Long id, Long parentId, String name) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("parentId", parentId);
        item.put("name", name);
        return item;
    }

    private Map<String, Object> product(Long id, Long categoryId, String name, String description,
                                        double price, double originPrice, String unit, int stock, int sales, int auditStatus) {
        String img = image("product" + id);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("merchantId", 1L);
        item.put("categoryId", categoryId);
        item.put("categoryName", categoryId == 1 ? "水果" : categoryId == 2 ? "蔬菜" : "有机食品");
        item.put("name", name);
        item.put("description", description);
        item.put("price", BigDecimal.valueOf(price));
        item.put("originalPrice", BigDecimal.valueOf(originPrice));
        item.put("stock", stock);
        item.put("unit", unit);
        item.put("sales", sales);
        item.put("mainImage", img);
        item.put("images", Arrays.asList(img, image("product-detail" + id)));
        item.put("auditStatus", auditStatus);
        return item;
    }

    private Map<String, Object> sampleOrder(Long id, String status, int adminStatus) {
        Map<String, Object> p1 = new LinkedHashMap<>();
        p1.put("id", 2L);
        p1.put("name", "精品圣女果");
        p1.put("images", Collections.singletonList(image("tomato")));
        p1.put("spec", "500g/盒");
        p1.put("price", 6.9);
        p1.put("quantity", 2);

        Map<String, Object> p2 = new LinkedHashMap<>();
        p2.put("id", 4L);
        p2.put("name", "红富士苹果礼盒");
        p2.put("images", Collections.singletonList(image("apple")));
        p2.put("spec", "2kg/箱");
        p2.put("price", 21.8);
        p2.put("quantity", 1);

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("id", id);
        order.put("userId", 1L);
        order.put("merchantId", 1L);
        order.put("orderNumber", "FV202602" + (1000 + id));
        order.put("totalAmount", 35.6);
        order.put("status", status);
        order.put("adminStatus", adminStatus);
        order.put("paymentMethod", "微信支付");
        order.put("createTime", now());
        order.put("paymentTime", "pending".equals(status) ? null : now());
        order.put("deliveryTime", "delivered".equals(status) || "completed".equals(status) ? now() : null);
        order.put("receiveTime", "completed".equals(status) ? now() : null);
        order.put("receiverName", "测试用户");
        order.put("receiverPhone", "13800138000");
        order.put("address", "郑州市金水区农业路 88 号");
        order.put("logisticsCompany", "郑州冷链快配");
        order.put("logisticsNo", "ZZL202602" + (2000 + id));
        List<Map<String, Object>> tracks = new ArrayList<>();
        tracks.add(logisticsTrack("订单创建", "订单已创建，等待支付"));
        if (!"pending".equals(status)) {
            tracks.add(logisticsTrack("支付成功", "支付完成，等待商家出库"));
            tracks.add(logisticsTrack("待发货", "商家已接单，正在打包"));
        }
        if ("delivered".equals(status) || "completed".equals(status)) {
            tracks.add(logisticsTrack("运输中", "冷链运输中，预计当天送达"));
        }
        if ("completed".equals(status)) {
            tracks.add(logisticsTrack("订单完成", "用户已确认收货"));
        }
        order.put("logisticsTracks", tracks);
        order.put("products", Arrays.asList(p1, p2));
        order.put("reviewed", false);
        return order;
    }

    private Map<String, Object> trace(Long productId, String productName) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("productId", productId);
        trace.put("productName", productName);
        trace.put("originName", "郑州近郊示范农场");
        trace.put("originAddress", "郑州市中牟县现代农业基地");
        trace.put("plantMethod", "绿色种植");
        trace.put("plantTime", "2026-01-12 08:00:00");
        trace.put("harvestTime", "2026-02-22 06:30:00");
        trace.put("storageCondition", "0-6℃冷藏，避免阳光直射");
        trace.put("shelfLife", 5);
        trace.put("testReport", image("report-" + productId));
        trace.put("auditStatus", 1);
        trace.put("auditRemark", "");
        trace.put("auditTime", now());
        trace.put("timeline", Arrays.asList(
                timeline("播种", "2026-01-12 08:00:00", "完成育苗和播种"),
                timeline("日常养护", "2026-02-02 10:00:00", "完成病虫害巡检，记录合格"),
                timeline("采收", "2026-02-22 06:30:00", "人工分拣采收，质量复检通过"),
                timeline("发货", now(), "冷链装车发往郑州市区")
        ));
        return trace;
    }

    private Map<String, Object> circlePost(Long id, String title, String content, List<String> images, List<Long> productIds) {
        Map<String, Object> post = new LinkedHashMap<>();
        post.put("id", id);
        post.put("merchantId", 1L);
        post.put("merchantName", "绿源果蔬店");
        post.put("title", title);
        post.put("content", content);
        post.put("images", images);
        post.put("productIds", productIds);
        post.put("viewCount", 100 + (int) (id * 28));
        post.put("likeCount", 20 + (int) (id * 3));
        post.put("commentCount", 1);
        post.put("createTime", now());
        post.put("comments", new ArrayList<>(Collections.singletonList(comment(1L, "小李", "看起来很新鲜，已下单！"))));
        return post;
    }

    private Map<String, Object> comment(Long id, String nickname, String content) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("nickname", nickname);
        item.put("content", content);
        item.put("createTime", now());
        return item;
    }

    private Map<String, Object> traceTemplate(Long id, String name, String description) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("description", description);
        row.put("fields", Arrays.asList(
                "originName", "originAddress", "plantMethod", "plantTime",
                "harvestTime", "storageCondition", "shelfLife", "testReport"
        ));
        row.put("updateTime", now());
        return row;
    }

    private Map<String, Object> timeline(String stage, String time, String desc) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("stage", stage);
        item.put("time", time);
        item.put("description", desc);
        return item;
    }

    private Map<String, Object> spec(Long id, String name, double price) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("price", BigDecimal.valueOf(price));
        return item;
    }

    private List<String> castListString(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<Long> castListLong(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .map(item -> {
                        if (item instanceof Number) {
                            return ((Number) item).longValue();
                        }
                        try {
                            return Long.parseLong(String.valueOf(item));
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> castListMap(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> list = (List<?>) value;
        return list.stream()
                .filter(Map.class::isInstance)
                .map(item -> new LinkedHashMap<>((Map<String, Object>) item))
                .collect(Collectors.toList());
    }

    private void appendLogisticsTrack(Map<String, Object> order, String status, String desc) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tracks = (List<Map<String, Object>>) order.computeIfAbsent("logisticsTracks", k -> new ArrayList<>());
        tracks.add(logisticsTrack(status, desc));
    }

    private String now() {
        return LocalDateTime.now().format(DF);
    }

    private String image(String tag) {
        String directUrl = IMAGE_URLS.get(tag);
        if (directUrl != null) {
            return directUrl;
        }
        return "/api/images/VCG211560306770.jpeg";
    }

    private Map<String, Object> logisticsTrack(String status, String description) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("status", status);
        item.put("description", description);
        item.put("time", now());
        return item;
    }

    private Long getMerchantIdByUserId(Long userId) {
        Map<String, Object> merchant = merchantByUserId.get(userId);
        if (merchant != null) {
            return ((Number) merchant.get("id")).longValue();
        }
        return 1L;
    }

    private String getMerchantNameByUserId(Long userId) {
        Map<String, Object> merchant = merchantByUserId.get(userId);
        if (merchant != null && merchant.get("name") != null) {
            return String.valueOf(merchant.get("name"));
        }
        return "绿源果蔬店";
    }

    private String getMerchantNameById(Long merchantId) {
        return merchants.stream()
                .filter(m -> Objects.equals(((Number) m.get("id")).longValue(), merchantId))
                .map(m -> String.valueOf(m.getOrDefault("shopName", m.get("name"))))
                .findFirst()
                .orElse("未知商家");
    }

    public void persistStateSilently() {
        try {
            persistState();
        } catch (Exception ignored) {
        }
    }

    private boolean loadStateFromDb() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS biz_runtime_state ("
                            + "state_key VARCHAR(64) PRIMARY KEY,"
                            + "state_json LONGTEXT NOT NULL,"
                            + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            List<String> rows = jdbcTemplate.query(
                    "SELECT state_json FROM biz_runtime_state WHERE state_key = ? LIMIT 1",
                    ps -> ps.setString(1, STATE_KEY),
                    (rs, rowNum) -> rs.getString(1)
            );
            if (rows == null || rows.isEmpty() || rows.get(0) == null || rows.get(0).trim().isEmpty()) {
                return false;
            }
            Map<String, Object> state = objectMapper.readValue(rows.get(0), new TypeReference<Map<String, Object>>() {
            });
            restoreState(state);
            normalizeStateImageUrls();
            persistStateSilently();
            return !categories.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void normalizeStateImageUrls() {
        for (Map<String, Object> product : products) {
            product.put("image", normalizeImageUrl(product.get("image")));
            product.put("mainImage", normalizeImageUrl(product.get("mainImage")));
            product.put("main_image", normalizeImageUrl(product.get("main_image")));
            product.put("images", normalizeImageList(product.get("images")));
        }
        for (Map<String, Object> merchant : merchants) {
            merchant.put("logoUrl", normalizeImageUrl(merchant.get("logoUrl")));
            merchant.put("shopLogo", normalizeImageUrl(merchant.get("shopLogo")));
            merchant.put("shop_logo", normalizeImageUrl(merchant.get("shop_logo")));
        }
        for (Map<String, Object> banner : banners) {
            banner.put("imageUrl", normalizeImageUrl(banner.get("imageUrl")));
            banner.put("image_url", normalizeImageUrl(banner.get("image_url")));
        }
        for (Map<String, Object> post : circlePosts) {
            post.put("images", normalizeImageList(post.get("images")));
        }
        for (Map<String, Object> trace : traceByProduct.values()) {
            trace.put("testReport", normalizeImageUrl(trace.get("testReport")));
            trace.put("test_report", normalizeImageUrl(trace.get("test_report")));
        }
        for (Map<String, Object> order : userOrders) {
            Object productsObj = order.get("products");
            if (productsObj instanceof List) {
                for (Object p : (List<?>) productsObj) {
                    if (p instanceof Map) {
                        Map<String, Object> item = (Map<String, Object>) p;
                        item.put("image", normalizeImageUrl(item.get("image")));
                        item.put("mainImage", normalizeImageUrl(item.get("mainImage")));
                    }
                }
            }
        }
    }

    private List<String> normalizeImageList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (Object item : (List<?>) value) {
            normalized.add(normalizeImageUrl(item));
        }
        return normalized;
    }

    private String normalizeImageUrl(Object raw) {
        String value = raw == null ? "" : String.valueOf(raw).trim();
        if (value.isEmpty()) {
            return "/api/images/VCG211560306770.jpeg";
        }
        if (value.startsWith("/api/images/")) {
            return value;
        }
        Matcher matcher = LEGACY_VCG_PATTERN.matcher(value);
        if (matcher.matches()) {
            return "/api/images/" + matcher.group(1);
        }
        if (value.contains("unsplash.com")) {
            return "/api/images/VCG211583441112.webp";
        }
        return "/api/images/VCG211560306770.jpeg";
    }

    private void persistState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("categories", categories);
        state.put("products", products);
        state.put("merchants", merchants);
        state.put("userOrders", userOrders);
        state.put("complaints", complaints);
        state.put("banners", banners);
        state.put("notices", notices);
        state.put("circlePosts", circlePosts);
        state.put("traceTemplates", traceTemplates);
        state.put("traceByProduct", traceByProduct);
        state.put("aiHistoryByUser", aiHistoryByUser);
        state.put("aiActiveSessionByUser", aiActiveSessionByUser);
        state.put("favoriteProductIdsByUser", favoriteProductIdsByUser);
        state.put("followMerchantIdsByUser", followMerchantIdsByUser);
        state.put("orderReviews", orderReviews);
        state.put("systemSettings", systemSettings);
        state.put("rolePermissions", rolePermissions);
        state.put("merchantByUserId", merchantByUserId);

        Map<String, Object> seq = new LinkedHashMap<>();
        seq.put("orderIdSeq", orderIdSeq.get());
        seq.put("merchantIdSeq", merchantIdSeq.get());
        seq.put("circlePostIdSeq", circlePostIdSeq.get());
        seq.put("complaintIdSeq", complaintIdSeq.get());
        seq.put("productIdSeq", productIdSeq.get());
        seq.put("bannerIdSeq", bannerIdSeq.get());
        seq.put("noticeIdSeq", noticeIdSeq.get());
        seq.put("traceTemplateIdSeq", traceTemplateIdSeq.get());
        seq.put("aiSessionIdSeq", aiSessionIdSeq.get());
        seq.put("aiMessageIdSeq", aiMessageIdSeq.get());
        state.put("sequences", seq);

        try {
            String json = objectMapper.writeValueAsString(state);
            jdbcTemplate.update(
                    "INSERT INTO biz_runtime_state(state_key, state_json, update_time) VALUES(?, ?, NOW()) "
                            + "ON DUPLICATE KEY UPDATE state_json = VALUES(state_json), update_time = NOW()",
                    STATE_KEY, json
            );
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreState(Map<String, Object> state) {
        categories.clear();
        categories.addAll(castListMap(state.get("categories")));
        products.clear();
        products.addAll(castListMap(state.get("products")));
        merchants.clear();
        merchants.addAll(castListMap(state.get("merchants")));
        userOrders.clear();
        userOrders.addAll(castListMap(state.get("userOrders")));
        complaints.clear();
        complaints.addAll(castListMap(state.get("complaints")));
        banners.clear();
        banners.addAll(castListMap(state.get("banners")));
        notices.clear();
        notices.addAll(castListMap(state.get("notices")));
        circlePosts.clear();
        circlePosts.addAll(castListMap(state.get("circlePosts")));
        traceTemplates.clear();
        traceTemplates.addAll(castListMap(state.get("traceTemplates")));
        orderReviews.clear();
        orderReviews.addAll(castListMap(state.get("orderReviews")));

        traceByProduct.clear();
        traceByProduct.putAll(convertMapLongMap(state.get("traceByProduct")));
        aiHistoryByUser.clear();
        aiHistoryByUser.putAll(convertMapLongListMap(state.get("aiHistoryByUser")));
        aiActiveSessionByUser.clear();
        aiActiveSessionByUser.putAll(convertMapLongLong(state.get("aiActiveSessionByUser")));
        favoriteProductIdsByUser.clear();
        favoriteProductIdsByUser.putAll(convertMapLongSetLong(state.get("favoriteProductIdsByUser")));
        followMerchantIdsByUser.clear();
        followMerchantIdsByUser.putAll(convertMapLongSetLong(state.get("followMerchantIdsByUser")));
        merchantByUserId.clear();
        merchantByUserId.putAll(convertMapLongMap(state.get("merchantByUserId")));

        systemSettings.clear();
        if (state.get("systemSettings") instanceof Map) {
            systemSettings.putAll((Map<String, Object>) state.get("systemSettings"));
        }

        rolePermissions.clear();
        if (state.get("rolePermissions") instanceof Map) {
            Map<String, Object> raw = (Map<String, Object>) state.get("rolePermissions");
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                Set<String> values = new LinkedHashSet<>();
                if (entry.getValue() instanceof List) {
                    for (Object item : (List<?>) entry.getValue()) {
                        values.add(String.valueOf(item));
                    }
                }
                rolePermissions.put(entry.getKey(), values);
            }
        }

        if (state.get("sequences") instanceof Map) {
            Map<String, Object> seq = (Map<String, Object>) state.get("sequences");
            orderIdSeq.set(toLong(seq.get("orderIdSeq")));
            merchantIdSeq.set(toLong(seq.get("merchantIdSeq")));
            circlePostIdSeq.set(toLong(seq.get("circlePostIdSeq")));
            complaintIdSeq.set(toLong(seq.get("complaintIdSeq")));
            productIdSeq.set(toLong(seq.get("productIdSeq")));
            bannerIdSeq.set(toLong(seq.get("bannerIdSeq")));
            noticeIdSeq.set(toLong(seq.get("noticeIdSeq")));
            traceTemplateIdSeq.set(toLong(seq.get("traceTemplateIdSeq")));
            aiSessionIdSeq.set(toLong(seq.get("aiSessionIdSeq")));
            aiMessageIdSeq.set(toLong(seq.get("aiMessageIdSeq")));
        }
    }

    private Map<Long, Map<String, Object>> convertMapLongMap(Object raw) {
        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        if (!(raw instanceof Map)) {
            return map;
        }
        Map<?, ?> source = (Map<?, ?>) raw;
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Long key = toLong(entry.getKey());
            if (entry.getValue() instanceof Map) {
                map.put(key, new LinkedHashMap<>((Map<String, Object>) entry.getValue()));
            }
        }
        return map;
    }

    private Map<Long, List<Map<String, Object>>> convertMapLongListMap(Object raw) {
        Map<Long, List<Map<String, Object>>> map = new LinkedHashMap<>();
        if (!(raw instanceof Map)) {
            return map;
        }
        Map<?, ?> source = (Map<?, ?>) raw;
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Long key = toLong(entry.getKey());
            List<Map<String, Object>> values = new ArrayList<>();
            if (entry.getValue() instanceof List) {
                for (Object item : (List<?>) entry.getValue()) {
                    if (item instanceof Map) {
                        values.add(new LinkedHashMap<>((Map<String, Object>) item));
                    }
                }
            }
            map.put(key, values);
        }
        return map;
    }

    private Map<Long, Set<Long>> convertMapLongSetLong(Object raw) {
        Map<Long, Set<Long>> map = new LinkedHashMap<>();
        if (!(raw instanceof Map)) {
            return map;
        }
        Map<?, ?> source = (Map<?, ?>) raw;
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Long key = toLong(entry.getKey());
            Set<Long> values = new LinkedHashSet<>();
            if (entry.getValue() instanceof List) {
                for (Object item : (List<?>) entry.getValue()) {
                    values.add(toLong(item));
                }
            } else if (entry.getValue() instanceof Set) {
                for (Object item : (Set<?>) entry.getValue()) {
                    values.add(toLong(item));
                }
            }
            map.put(key, values);
        }
        return map;
    }

    private Map<Long, Long> convertMapLongLong(Object raw) {
        Map<Long, Long> map = new LinkedHashMap<>();
        if (!(raw instanceof Map)) {
            return map;
        }
        Map<?, ?> source = (Map<?, ?>) raw;
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            map.put(toLong(entry.getKey()), toLong(entry.getValue()));
        }
        return map;
    }

    private Map<String, Object> banner(Long id, String title, String imageUrl, String targetUrl) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("title", title);
        item.put("imageUrl", imageUrl);
        item.put("targetUrl", targetUrl);
        return item;
    }

    private Map<String, Object> notice(Long id, String type, String title, String publishTime) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("type", type);
        item.put("title", title);
        item.put("content", title + "（详情请查看平台通知）");
        item.put("publishTime", publishTime);
        return item;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private double toDouble(Object value) {
        if (value == null) return 0D;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0D;
        }
    }
}
