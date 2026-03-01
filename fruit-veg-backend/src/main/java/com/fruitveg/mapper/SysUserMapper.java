package com.fruitveg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitveg.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表Mapper接口
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser selectByPhone(String phone);
}
