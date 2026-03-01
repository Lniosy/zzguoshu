package com.fruitveg.controller.common;

import com.fruitveg.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping
    public Result<Map<String, Object>> upload(MultipartFile file) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", file == null ? "" : file.getOriginalFilename());
        data.put("url", "/api/images/VCG211560306770.jpeg");
        return Result.success(data);
    }
}
