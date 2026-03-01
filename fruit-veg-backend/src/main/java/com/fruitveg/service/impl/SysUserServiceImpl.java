package com.fruitveg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitveg.entity.SysUser;
import com.fruitveg.mapper.SysUserMapper;
import com.fruitveg.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户表Service实现类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser getByPhone(String phone) {
        return sysUserMapper.selectByPhone(phone);
    }

    @Override
    public boolean register(SysUser user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认值
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 保存用户
        return save(user);
    }

    @Override
    public boolean resetPassword(String phone, String newPassword) {
        SysUser user = getByPhone(phone);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            return updateById(user);
        }
        return false;
    }
}
