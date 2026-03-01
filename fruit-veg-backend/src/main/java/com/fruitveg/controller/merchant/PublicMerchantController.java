package com.fruitveg.controller.merchant;

import com.fruitveg.common.Result;
import com.fruitveg.service.CatalogDbService;
import com.fruitveg.service.RuntimeDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/merchant/public")
public class PublicMerchantController {

    private final CatalogDbService catalogDbService;
    private final RuntimeDataService runtimeDataService;

    public PublicMerchantController(CatalogDbService catalogDbService, RuntimeDataService runtimeDataService) {
        this.catalogDbService = catalogDbService;
        this.runtimeDataService = runtimeDataService;
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> merchant = catalogDbService.getMerchantInfo(id);
        if (merchant == null) {
            merchant = runtimeDataService.getPublicMerchantInfo(id);
        }
        if (merchant == null) {
            return Result.error(404, "商家不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("merchant", merchant);
        result.put("products", catalogDbService.listProducts(1, 12, null, null, null, null, null, id));
        result.put("posts", runtimeDataService.listCirclePosts(null, 1, 20, false, id));
        return Result.success(result);
    }
}
