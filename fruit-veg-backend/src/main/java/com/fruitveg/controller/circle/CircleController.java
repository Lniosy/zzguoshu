package com.fruitveg.controller.circle;

import com.fruitveg.common.Result;
import com.fruitveg.service.RuntimeDataService;
import com.fruitveg.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/circle")
public class CircleController {

    private final RuntimeDataService mockDataService;
    private final JwtUtils jwtUtils;

    public CircleController(RuntimeDataService mockDataService, JwtUtils jwtUtils) {
        this.mockDataService = mockDataService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size,
                                             @RequestParam(required = false) Boolean followedOnly,
                                             @RequestParam(required = false) Long merchantId,
                                             HttpServletRequest request) {
        return Result.success(mockDataService.listCirclePosts(getUserIdOrNull(request), page, size, followedOnly, merchantId));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> post = mockDataService.getCirclePost(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        return Result.success(post);
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        return mockDataService.toggleCircleLike(id) ? Result.success() : Result.error(404, "动态不存在");
    }

    @PostMapping("/{id}/comment")
    public Result<Map<String, Object>> comment(@PathVariable Long id,
                                               @RequestBody Map<String, String> payload,
                                               HttpServletRequest request) {
        Long userId = getUserId(request);
        Map<String, Object> comment = mockDataService.addCircleComment(id, userId, payload.getOrDefault("content", ""));
        if (comment == null) {
            return Result.error(404, "动态不存在");
        }
        return Result.success(comment);
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long postId,
                                       @PathVariable Long commentId,
                                       HttpServletRequest request) {
        Long userId = getUserId(request);
        return mockDataService.deleteCircleComment(postId, commentId, userId)
                ? Result.success()
                : Result.error(404, "评论不存在或无权限");
    }

    @PostMapping("/follow/{merchantId}")
    public Result<Void> follow(@PathVariable Long merchantId, HttpServletRequest request) {
        return mockDataService.followMerchant(getUserId(request), merchantId)
                ? Result.success()
                : Result.error(404, "商家不存在");
    }

    @DeleteMapping("/follow/{merchantId}")
    public Result<Void> unfollow(@PathVariable Long merchantId, HttpServletRequest request) {
        return mockDataService.unfollowMerchant(getUserId(request), merchantId)
                ? Result.success()
                : Result.error(404, "关注关系不存在");
    }

    @GetMapping("/follow/merchants")
    public Result<Object> followedMerchants(HttpServletRequest request) {
        return Result.success(mockDataService.listFollowedMerchants(getUserId(request)));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return jwtUtils.getUserIdFromToken(token.substring(7));
        }
        return 1L;
    }

    private Long getUserIdOrNull(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                return jwtUtils.getUserIdFromToken(token.substring(7));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
