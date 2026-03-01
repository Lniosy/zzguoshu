package com.fruitveg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitveg.entity.SysUser;

/**
 * 用户表Service接口
 *
 * @author lniosy
 * @since 2026-02-27
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser getByPhone(String phone);

    /**
     * 注册用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean register(SysUser user);

    /**
     * 重置密码
     *
     * @param phone 手机号
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(String phone, String newPassword);
}
