package com.fruitveg.controller.content;

import com.fruitveg.common.Result;
import com.fruitveg.service.MockDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/content")
public class ContentController {

    private final MockDataService mockDataService;

    public ContentController(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }

    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> banners() {
        return Result.success(mockDataService.listBanners());
    }

    @GetMapping("/notices")
    public Result<List<Map<String, Object>>> notices() {
        return Result.success(mockDataService.listNotices());
    }
}

