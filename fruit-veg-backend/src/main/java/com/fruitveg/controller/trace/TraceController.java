package com.fruitveg.controller.trace;

import com.fruitveg.common.Result;
import com.fruitveg.service.CatalogDbService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/trace")
public class TraceController {

    private final CatalogDbService catalogDbService;

    public TraceController(CatalogDbService catalogDbService) {
        this.catalogDbService = catalogDbService;
    }

    @GetMapping("/detail/{productId}")
    public Result<Map<String, Object>> detail(@PathVariable Long productId) {
        Map<String, Object> trace = catalogDbService.getTraceDetail(productId);
        if (trace == null) {
            return Result.error(404, "溯源信息不存在");
        }
        return Result.success(trace);
    }
}
