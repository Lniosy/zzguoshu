package com.fruitveg.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitveg.entity.BizCategory;
import com.fruitveg.entity.BizMerchant;
import com.fruitveg.entity.BizProduct;
import com.fruitveg.entity.BizTrace;
import com.fruitveg.mapper.BizCategoryMapper;
import com.fruitveg.mapper.BizMerchantMapper;
import com.fruitveg.mapper.BizProductMapper;
import com.fruitveg.mapper.BizTraceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogDbService {

    private static final String DEFAULT_IMAGE = "/api/images/VCG211496684730.jpg";
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final BizCategoryMapper categoryMapper;
    private final BizProductMapper productMapper;
    private final BizMerchantMapper merchantMapper;
    private final BizTraceMapper traceMapper;
    private final ObjectMapper objectMapper;

    public CatalogDbService(BizCategoryMapper categoryMapper,
                            BizProductMapper productMapper,
                            BizMerchantMapper merchantMapper,
                            BizTraceMapper traceMapper,
                            ObjectMapper objectMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.merchantMapper = merchantMapper;
        this.traceMapper = traceMapper;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> listCategoryTree() {
        List<Map<String, Object>> categories = categoryMapper.selectMaps(
                new QueryWrapper<BizCategory>()
                        .eq("status", 1)
                        .orderByAsc("sort")
                        .orderByAsc("id")
        );

        List<Map<String, Object>> nodes = categories.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", getLong(row, "id"));
            item.put("parentId", getLong(row, "parent_id", "parentId"));
            item.put("name", String.valueOf(row.getOrDefault("name", "")));
            item.put("children", new ArrayList<>());
            return item;
        }).collect(Collectors.toList());

        Map<Long, Map<String, Object>> byId = nodes.stream()
                .collect(Collectors.toMap(node -> ((Number) node.get("id")).longValue(), node -> node));

        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            Long parentId = ((Number) node.get("parentId")).longValue();
            if (parentId == 0L) {
                roots.add(node);
            } else {
                Map<String, Object> parent = byId.get(parentId);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(node);
                }
            }
        }
        return roots;
    }

    public Map<String, Object> listProducts(Integer page, Integer size, String keyword, String sortBy, Long categoryId,
                                            Double minPrice, Double maxPrice, Long merchantId) {
        QueryWrapper<BizProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("name", keyword.trim());
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (merchantId != null) {
            wrapper.eq("merchant_id", merchantId);
        }
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }

        if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc("price");
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc("price");
        } else if ("sales_desc".equals(sortBy)) {
            wrapper.orderByDesc("sales");
        } else {
            wrapper.orderByDesc("id");
        }

        List<Map<String, Object>> dbRows = productMapper.selectMaps(wrapper);
        Map<Long, String> categoryNameMap = loadCategoryNameMap();
        Map<Long, Map<String, Object>> merchantMap = loadMerchantMap();

        List<Map<String, Object>> rows = dbRows.stream()
                .map(row -> toProductMap(row, categoryNameMap, merchantMap))
                .collect(Collectors.toList());

        int current = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 12 : size;
        int from = Math.min((current - 1) * pageSize, rows.size());
        int to = Math.min(from + pageSize, rows.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", rows.subList(from, to));
        result.put("total", rows.size());
        result.put("current", current);
        result.put("size", pageSize);
        return result;
    }

    public List<Map<String, Object>> recommendProducts() {
        List<Map<String, Object>> products = productMapper.selectMaps(
                new QueryWrapper<BizProduct>()
                        .eq("status", 1)
                        .orderByDesc("sales")
                        .last("limit 6")
        );
        Map<Long, Map<String, Object>> merchantMap = loadMerchantMap();
        return products.stream().map(row -> {
            Map<String, Object> result = new LinkedHashMap<>();
            Long id = getLong(row, "id");
            Long merchantId = getLong(row, "merchant_id", "merchantId");
            Map<String, Object> merchant = merchantMap.getOrDefault(merchantId, defaultMerchant(merchantId));
            result.put("id", id);
            result.put("name", String.valueOf(row.getOrDefault("name", "")));
            result.put("description", String.valueOf(row.getOrDefault("description", "")));
            result.put("merchantId", merchantId);
            result.put("merchantName", merchant.get("shopName"));
            result.put("merchantAvatar", merchant.get("shopLogo"));
            result.put("merchantAddress", merchant.get("address"));
            result.put("price", toBigDecimal(row.get("price")));
            result.put("originalPrice", toBigDecimal(row.getOrDefault("original_price", row.get("originalPrice"))));
            result.put("image", firstNonBlank(
                    String.valueOf(row.getOrDefault("main_image", "")),
                    String.valueOf(row.getOrDefault("mainImage", "")),
                    DEFAULT_IMAGE
            ));
            return result;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getProduct(Long id) {
        Map<String, Object> row = productMapper.selectMaps(
                new QueryWrapper<BizProduct>().eq("id", id).eq("status", 1).last("limit 1")
        ).stream().findFirst().orElse(null);

        if (row == null) {
            return null;
        }

        Map<Long, String> categoryNameMap = loadCategoryNameMap();
        Map<Long, Map<String, Object>> merchantMap = loadMerchantMap();
        Map<String, Object> result = toProductMap(row, categoryNameMap, merchantMap);

        BigDecimal basePrice = toBigDecimal(row.get("price"));
        List<Map<String, Object>> specs = new ArrayList<>();
        specs.add(spec(1L, "500g/份", basePrice));
        specs.add(spec(2L, "1kg/份", basePrice.multiply(BigDecimal.valueOf(1.88))));
        result.put("specs", specs);

        Long merchantId = getLong(row, "merchant_id", "merchantId");
        Map<String, Object> merchant = merchantMap.getOrDefault(merchantId, defaultMerchant(merchantId));
        Map<String, Object> shop = new LinkedHashMap<>(merchant);
        result.put("shop", shop);
        return result;
    }

    public Map<String, Object> getMerchantInfo(Long merchantId) {
        Map<String, Object> merchant = loadMerchantMap().get(merchantId);
        if (merchant == null) {
            return null;
        }
        return new LinkedHashMap<>(merchant);
    }

    public Map<String, Object> getTraceDetail(Long productId) {
        Map<String, Object> trace = traceMapper.selectMaps(
                new QueryWrapper<BizTrace>().eq("product_id", productId).last("limit 1")
        ).stream().findFirst().orElse(null);
        if (trace == null) {
            return null;
        }

        Map<String, Object> product = productMapper.selectMaps(
                new QueryWrapper<BizProduct>().eq("id", productId).last("limit 1")
        ).stream().findFirst().orElse(null);

        String plantTime = formatDate(trace.get("plant_time"));
        String harvestTime = formatDate(trace.get("harvest_time"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("productName", product == null ? "果蔬商品" : String.valueOf(product.getOrDefault("name", "果蔬商品")));
        result.put("originName", String.valueOf(trace.getOrDefault("origin_name", "")));
        result.put("originAddress", String.valueOf(trace.getOrDefault("origin_address", "")));
        result.put("plantMethod", String.valueOf(trace.getOrDefault("plant_method", "")));
        result.put("plantTime", plantTime);
        result.put("harvestTime", harvestTime);
        result.put("storageCondition", String.valueOf(trace.getOrDefault("storage_condition", "")));
        result.put("shelfLife", toInt(trace.get("shelf_life"), 0));
        result.put("testReport", String.valueOf(trace.getOrDefault("test_report", DEFAULT_IMAGE)));
        result.put("timeline", Arrays.asList(
                timeline("播种", plantTime, "基地完成播种并建立批次档案"),
                timeline("日常养护", plantTime, "完成农事巡检与抽样检测"),
                timeline("采收", harvestTime, "按批次完成分级采收"),
                timeline("入仓待发", formatDate(new Date()), "已进入冷链仓，待订单发货")
        ));
        return result;
    }

    private Map<String, Object> toProductMap(Map<String, Object> row, Map<Long, String> categoryNameMap, Map<Long, Map<String, Object>> merchantMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        Long id = getLong(row, "id");
        Long merchantId = getLong(row, "merchant_id", "merchantId");
        Long categoryId = getLong(row, "category_id", "categoryId");

        String mainImage = firstNonBlank(
                String.valueOf(row.getOrDefault("main_image", "")),
                String.valueOf(row.getOrDefault("mainImage", "")),
                DEFAULT_IMAGE
        );

        result.put("id", id);
        result.put("merchantId", merchantId);
        result.put("categoryId", categoryId);
        result.put("categoryName", categoryNameMap.getOrDefault(categoryId, "未分类"));
        result.put("name", String.valueOf(row.getOrDefault("name", "")));
        result.put("description", String.valueOf(row.getOrDefault("description", "")));
        result.put("price", toBigDecimal(row.get("price")));
        result.put("originalPrice", toBigDecimal(row.getOrDefault("original_price", row.get("originalPrice"))));
        result.put("stock", toInt(row.get("stock"), 0));
        result.put("unit", firstNonBlank(String.valueOf(row.getOrDefault("unit", "")), "份"));
        result.put("sales", toInt(row.get("sales"), 0));
        result.put("mainImage", mainImage);
        result.put("images", parseImages(String.valueOf(row.getOrDefault("images", "")), mainImage));
        result.put("auditStatus", toInt(row.get("status"), 0) == 1 ? 1 : 0);
        Map<String, Object> merchant = merchantMap.getOrDefault(merchantId, defaultMerchant(merchantId));
        result.put("merchantName", merchant.get("shopName"));
        result.put("merchantAvatar", merchant.get("shopLogo"));
        result.put("merchantAddress", merchant.get("address"));
        return result;
    }

    private List<String> parseImages(String imagesJson, String mainImage) {
        if (imagesJson != null && !imagesJson.trim().isEmpty()) {
            try {
                List<String> list = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
                });
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (Exception ignored) {
            }
        }
        return Collections.singletonList(firstNonBlank(mainImage, DEFAULT_IMAGE));
    }

    private Map<Long, String> loadCategoryNameMap() {
        return categoryMapper.selectMaps(new QueryWrapper<BizCategory>().eq("status", 1))
                .stream()
                .collect(Collectors.toMap(
                        row -> getLong(row, "id"),
                        row -> String.valueOf(row.getOrDefault("name", "")),
                        (a, b) -> a
                ));
    }

    private Map<Long, String> loadMerchantNameMap() {
        return loadMerchantMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, item -> String.valueOf(item.getValue().get("shopName"))));
    }

    private Map<Long, Map<String, Object>> loadMerchantMap() {
        return merchantMapper.selectMaps(new QueryWrapper<BizMerchant>().eq("status", 1))
                .stream()
                .collect(Collectors.toMap(
                        row -> getLong(row, "id"),
                        row -> {
                            Long id = getLong(row, "id");
                            Map<String, Object> merchant = new LinkedHashMap<>();
                            merchant.put("id", id);
                            merchant.put("shopName", firstNonBlank(
                                    String.valueOf(row.getOrDefault("shop_name", "")),
                                    String.valueOf(row.getOrDefault("shopName", "")),
                                    "绿源果蔬店"
                            ));
                            merchant.put("shopLogo", firstNonBlank(
                                    String.valueOf(row.getOrDefault("shop_logo", "")),
                                    String.valueOf(row.getOrDefault("shopLogo", "")),
                                    DEFAULT_IMAGE
                            ));
                            merchant.put("shopDesc", firstNonBlank(
                                    String.valueOf(row.getOrDefault("shop_desc", "")),
                                    String.valueOf(row.getOrDefault("shopDesc", "")),
                                    "冷链直达，48小时内送达"
                            ));
                            merchant.put("address", firstNonBlank(
                                    String.valueOf(row.getOrDefault("address", "")),
                                    "郑州市"
                            ));
                            merchant.put("rating", 4.8);
                            return merchant;
                        },
                        (a, b) -> a
                ));
    }

    private Map<String, Object> defaultMerchant(Long merchantId) {
        Map<String, Object> merchant = new LinkedHashMap<>();
        merchant.put("id", merchantId);
        merchant.put("shopName", "绿源果蔬店");
        merchant.put("shopLogo", DEFAULT_IMAGE);
        merchant.put("shopDesc", "冷链直达，48小时内送达");
        merchant.put("address", "郑州市");
        merchant.put("rating", 4.8);
        return merchant;
    }

    private Map<String, Object> spec(Long id, String name, BigDecimal price) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("price", price.setScale(2, RoundingMode.HALF_UP));
        return item;
    }

    private Map<String, Object> timeline(String stage, String time, String description) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("stage", stage);
        item.put("time", time);
        item.put("description", description);
        return item;
    }

    private String formatDate(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date) {
            return DF.format((Date) value);
        }
        return String.valueOf(value);
    }

    private Long getLong(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
                try {
                    return Long.parseLong(String.valueOf(value));
                } catch (Exception ignored) {
                }
            }
        }
        return 0L;
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.trim().isEmpty()) {
                return candidate;
            }
        }
        return "";
    }
}
