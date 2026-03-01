package com.fruitveg.controller.trace;

import com.fruitveg.common.Result;
import com.fruitveg.service.MockDataService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/trace")
public class TraceController {

    private final MockDataService mockDataService;

    public TraceController(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }

    @GetMapping("/detail/{productId}")
    public Result<Map<String, Object>> detail(@PathVariable Long productId) {
        Map<String, Object> trace = mockDataService.getTraceDetail(productId);
        if (trace == null) {
            return Result.error(404, "溯源信息不存在");
        }
        return Result.success(trace);
    }
}
