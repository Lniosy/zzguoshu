package com.fruitveg.controller.user;

import com.fruitveg.common.Result;
import com.fruitveg.service.RuntimeDataService;
import com.fruitveg.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    private final RuntimeDataService mockDataService;
    private final JwtUtils jwtUtils;

    public FavoriteController(RuntimeDataService mockDataService, JwtUtils jwtUtils) {
        this.mockDataService = mockDataService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(HttpServletRequest request) {
        return Result.success(mockDataService.listUserFavorites(getUserId(request)));
    }

    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId, HttpServletRequest request) {
        return mockDataService.addUserFavorite(getUserId(request), productId)
                ? Result.success()
                : Result.error(404, "商品不存在");
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId, HttpServletRequest request) {
        return mockDataService.removeUserFavorite(getUserId(request), productId)
                ? Result.success()
                : Result.error(404, "收藏不存在");
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return jwtUtils.getUserIdFromToken(token.substring(7));
        }
        return 1L;
    }
}

