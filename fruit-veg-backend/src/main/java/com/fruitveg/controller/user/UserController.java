package com.fruitveg.controller.user;

import com.fruitveg.common.Result;
import com.fruitveg.entity.BizUserAddress;
import com.fruitveg.service.UserService;
import com.fruitveg.utils.JwtUtils;
import com.fruitveg.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;

/**
 * 用户中心控制器
 *
 * @author lniosy
 * @since 2026-02-28
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            UserInfoVO userInfoVO = userService.getUserInfo(userId);
            return Result.success(userInfoVO);
        }
        return Result.error("用户未登录或Token无效");
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody @Validated UserInfoVO userInfoVO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            userService.updateUserInfo(userId, userInfoVO);
            return Result.success();
        }
        return Result.error("用户未登录或Token无效");
    }

    @GetMapping("/address")
    public Result<List<BizUserAddress>> getAddresses(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            List<BizUserAddress> addresses = userService.getUserAddresses(userId);
            return Result.success(addresses);
        }
        return Result.error("用户未登录或Token无效");
    }

    @PostMapping("/address")
    public Result<Void> addAddress(@RequestBody @Validated BizUserAddress address, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            userService.addUserAddress(userId, address);
            return Result.success();
        }
        return Result.error("用户未登录或Token无效");
    }

    @PutMapping("/address/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody @Validated BizUserAddress address, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            userService.updateUserAddress(userId, id, address);
            return Result.success();
        }
        return Result.error("用户未登录或Token无效");
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            userService.deleteUserAddress(userId, id);
            return Result.success();
        }
        return Result.error("用户未登录或Token无效");
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtils.getUserIdFromToken(token);
            userService.updatePassword(userId,
                    payload.getOrDefault("oldPassword", ""),
                    payload.getOrDefault("newPassword", ""));
            return Result.success();
        }
        return Result.error("用户未登录或Token无效");
    }
}
