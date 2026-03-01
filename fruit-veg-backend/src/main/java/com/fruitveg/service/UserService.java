package com.fruitveg.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitveg.entity.BizUserAddress;
import com.fruitveg.entity.SysUser;
import com.fruitveg.vo.UserInfoVO;

/**
 * 用户服务接口
 *
 * @author lniosy
 * @since 2026-02-28
 */
public interface UserService extends IService<SysUser> {

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 修改用户信息
     *
     * @param userId 用户ID
     * @param userInfoVO 用户信息
     */
    void updateUserInfo(Long userId, UserInfoVO userInfoVO);

    /**
     * 获取用户收货地址列表
     *
     * @param userId 用户ID
     * @return 收货地址列表
     */
    java.util.List<BizUserAddress> getUserAddresses(Long userId);

    /**
     * 添加收货地址
     *
     * @param userId 用户ID
     * @param address 收货地址信息
     */
    void addUserAddress(Long userId, BizUserAddress address);

    /**
     * 修改收货地址
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     * @param address 收货地址信息
     */
    void updateUserAddress(Long userId, Long addressId, BizUserAddress address);

    /**
     * 删除收货地址
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     */
    void deleteUserAddress(Long userId, Long addressId);

    /**
     * 修改当前用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
