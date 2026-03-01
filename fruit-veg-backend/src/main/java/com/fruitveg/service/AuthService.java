package com.fruitveg.service;

import com.fruitveg.vo.AuthVO;
import com.fruitveg.vo.LoginCodeVO;
import com.fruitveg.vo.LoginVO;
import com.fruitveg.vo.RegisterVO;

/**
 * 认证服务接口
 *
 * @author lniosy
 * @since 2026-02-28
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param registerVO 注册信息
     * @return 认证结果（包含token）
     */
    AuthVO register(RegisterVO registerVO);

    /**
     * 用户登录
     *
     * @param loginVO 登录信息
     * @return 认证结果（包含token）
     */
    AuthVO login(LoginVO loginVO);

    /**
     * 手机验证码登录（演示版）
     *
     * @param loginCodeVO 登录信息
     * @return 认证结果
     */
    AuthVO loginByCode(LoginCodeVO loginCodeVO);
}
