package com.fruitveg.vo;

import lombok.Data;

/**
 * 认证结果VO
 *
 * @author lniosy
 * @since 2026-02-28
 */
@Data
public class AuthVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfoVO userInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoVO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoVO userInfo) {
        this.userInfo = userInfo;
    }
}
