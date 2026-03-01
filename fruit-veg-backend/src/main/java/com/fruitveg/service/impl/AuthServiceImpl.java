package com.fruitveg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruitveg.entity.BizMerchant;
import com.fruitveg.entity.SysUser;
import com.fruitveg.mapper.BizMerchantMapper;
import com.fruitveg.mapper.SysUserMapper;
import com.fruitveg.service.AuthService;
import com.fruitveg.utils.JwtUtils;
import com.fruitveg.vo.AuthVO;
import com.fruitveg.vo.LoginCodeVO;
import com.fruitveg.vo.LoginVO;
import com.fruitveg.vo.RegisterVO;
import com.fruitveg.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 认证服务实现类
 *
 * @author lniosy
 * @since 2026-02-28
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private BizMerchantMapper bizMerchantMapper;

    @Override
    public AuthVO register(RegisterVO registerVO) {
        // 检查用户是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, registerVO.getPhone());
        SysUser existingUser = sysUserMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new RuntimeException("该手机号已注册");
        }

        // 创建新用户
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(registerVO, sysUser);
        // 密码加密
        sysUser.setPassword(bCryptPasswordEncoder.encode(registerVO.getPassword()));
        // 基础字段兜底，避免出现空值导致插入失败
        sysUser.setUsername(registerVO.getPhone());
        sysUser.setNickname(registerVO.getNickname() == null || registerVO.getNickname().isEmpty() ? "用户" + registerVO.getPhone().substring(7) : registerVO.getNickname());
        sysUser.setCreateTime(new Date());
        sysUser.setUpdateTime(new Date());
        // 设置默认值
        sysUser.setStatus(1); // 正常状态
        sysUser.setGender(0); // 未知性别
        sysUserMapper.insert(sysUser);

        // 生成token
        String role = resolveRole(sysUser);
        String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getUsername(), role);

        // 转换用户信息
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(sysUser, userInfoVO);
        userInfoVO.setRole(role);

        // 返回结果
        AuthVO authVO = new AuthVO();
        authVO.setToken(token);
        authVO.setUserInfo(userInfoVO);
        return authVO;
    }

    @Override
    public AuthVO login(LoginVO loginVO) {
        // 根据手机号或用户名查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq(SysUser::getPhone, loginVO.getAccount())
                .or().eq(SysUser::getUsername, loginVO.getAccount()));
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        if (sysUser == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!bCryptPasswordEncoder.matches(loginVO.getPassword(), sysUser.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (sysUser.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成token
        String role = resolveRole(sysUser);
        String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getUsername(), role);

        // 转换用户信息
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(sysUser, userInfoVO);
        userInfoVO.setRole(role);

        // 返回结果
        AuthVO authVO = new AuthVO();
        authVO.setToken(token);
        authVO.setUserInfo(userInfoVO);
        return authVO;
    }

    @Override
    public AuthVO loginByCode(LoginCodeVO loginCodeVO) {
        if (!"123456".equals(loginCodeVO.getCode())) {
            throw new RuntimeException("验证码错误");
        }
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, loginCodeVO.getPhone());
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }
        if (sysUser.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        String role = resolveRole(sysUser);
        String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getUsername(), role);

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(sysUser, userInfoVO);
        userInfoVO.setRole(role);

        AuthVO authVO = new AuthVO();
        authVO.setToken(token);
        authVO.setUserInfo(userInfoVO);
        return authVO;
    }

    private String resolveRole(SysUser sysUser) {
        if ("admin".equalsIgnoreCase(sysUser.getUsername())) {
            return "ADMIN";
        }
        BizMerchant merchant = bizMerchantMapper.selectByUserId(sysUser.getId());
        if (merchant != null && merchant.getStatus() != null && merchant.getStatus() == 1) {
            return "MERCHANT";
        }
        return "USER";
    }
}
