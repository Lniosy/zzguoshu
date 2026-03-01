package com.fruitveg.controller.auth;

import com.fruitveg.common.Result;
import com.fruitveg.service.AuthService;
import com.fruitveg.vo.AuthVO;
import com.fruitveg.vo.LoginCodeVO;
import com.fruitveg.vo.LoginVO;
import com.fruitveg.vo.RegisterVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author lniosy
 * @since 2026-02-28
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<AuthVO> register(@RequestBody @Validated RegisterVO registerVO) {
        logger.debug("AuthController.register - Received register request: phone={}", registerVO.getPhone());

        try {
            AuthVO authVO = authService.register(registerVO);
            logger.debug("AuthController.register - Register successful: phone={}", registerVO.getPhone());
            return Result.success(authVO);
        } catch (Exception e) {
            logger.error("AuthController.register - Register failed: " + e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<AuthVO> login(@RequestBody @Validated LoginVO loginVO) {
        logger.debug("AuthController.login - Received login request: account={}", loginVO.getAccount());

        try {
            AuthVO authVO = authService.login(loginVO);
            logger.debug("AuthController.login - Login successful: account={}", loginVO.getAccount());
            return Result.success(authVO);
        } catch (Exception e) {
            logger.error("AuthController.login - Login failed: " + e.getMessage(), e);
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/login/code")
    public Result<AuthVO> loginByCode(@RequestBody @Validated LoginCodeVO loginCodeVO) {
        try {
            return Result.success(authService.loginByCode(loginCodeVO));
        } catch (Exception e) {
            logger.error("AuthController.loginByCode - Login failed: " + e.getMessage(), e);
            return Result.error(401, e.getMessage());
        }
    }

    @GetMapping("/code")
    public Result<String> sendCode(@RequestParam String phone) {
        logger.debug("mock send verify code for {}", phone);
        return Result.success("123456");
    }
}
