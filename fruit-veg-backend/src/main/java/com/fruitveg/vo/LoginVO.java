package com.fruitveg.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求VO
 *
 * @author lniosy
 * @since 2026-02-28
 */
@Data
public class LoginVO {

    /**
     * 账号（手机号或用户名）
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
