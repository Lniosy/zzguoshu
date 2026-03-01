package com.fruitveg.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentDbService {

    private final JdbcTemplate jdbcTemplate;

    public ContentDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listBanners() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, title, image_url, target_url FROM biz_banner WHERE status = 1 ORDER BY sort ASC, id ASC"
        );
        return rows.stream().map(row -> {
            row.put("imageUrl", row.get("image_url"));
            row.put("targetUrl", row.get("target_url"));
            row.remove("image_url");
            row.remove("target_url");
            return row;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listNotices() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, type, title, content, publish_time FROM biz_notice WHERE status = 1 ORDER BY publish_time DESC, id DESC"
        );
        return rows.stream().map(row -> {
            row.put("publishTime", row.get("publish_time"));
            row.remove("publish_time");
            return row;
        }).collect(Collectors.toList());
    }
}
