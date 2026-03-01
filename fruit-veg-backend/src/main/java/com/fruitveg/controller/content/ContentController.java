package com.fruitveg.controller.content;

import com.fruitveg.common.Result;
import com.fruitveg.service.ContentDbService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/content")
public class ContentController {

    private final ContentDbService contentDbService;

    public ContentController(ContentDbService contentDbService) {
        this.contentDbService = contentDbService;
    }

    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> banners() {
        return Result.success(contentDbService.listBanners());
    }

    @GetMapping("/notices")
    public Result<List<Map<String, Object>>> notices() {
        return Result.success(contentDbService.listNotices());
    }
}
