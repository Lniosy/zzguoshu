package com.fruitveg.controller.product;

import com.fruitveg.common.Result;
import com.fruitveg.service.CatalogDbService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ProductController {

    private final CatalogDbService catalogDbService;

    public ProductController(CatalogDbService catalogDbService) {
        this.catalogDbService = catalogDbService;
    }

    @GetMapping("/category/list")
    public Result<List<Map<String, Object>>> getCategoryList() {
        return Result.success(catalogDbService.listCategoryTree());
    }

    @GetMapping("/category/tree")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(catalogDbService.listCategoryTree());
    }

    @GetMapping("/product/list")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return Result.success(catalogDbService.listProducts(page, size, keyword, sortBy, category, minPrice, maxPrice));
    }

    @GetMapping("/product/recommend")
    public Result<List<Map<String, Object>>> getRecommendProducts() {
        return Result.success(catalogDbService.recommendProducts());
    }

    @GetMapping("/product/{id}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Map<String, Object> product = catalogDbService.getProduct(id);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(product);
    }

    @GetMapping("/product/search")
    public Result<Map<String, Object>> searchProduct(@RequestParam String keyword) {
        return Result.success(catalogDbService.listProducts(1, 12, keyword, null, null, null, null));
    }
}
