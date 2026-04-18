package com.fruitveg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitveg.entity.BizUserAddress;
import com.fruitveg.entity.BizMerchant;
import com.fruitveg.entity.SysUser;
import com.fruitveg.mapper.BizMerchantMapper;
import com.fruitveg.mapper.BizUserAddressMapper;
import com.fruitveg.mapper.SysUserMapper;
import com.fruitveg.service.UserService;
import com.fruitveg.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author lniosy
 * @since 2026-02-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BizUserAddressMapper bizUserAddressMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BizMerchantMapper bizMerchantMapper;

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(sysUser, userInfoVO);
        userInfoVO.setRole(resolveRole(sysUser));
        return userInfoVO;
    }

    @Override
    public void updateUserInfo(Long userId, UserInfoVO userInfoVO) {
        SysUser sysUser = new SysUser();
        sysUser.setId(userId);
        BeanUtils.copyProperties(userInfoVO, sysUser);
        sysUserMapper.updateById(sysUser);
    }

    @Override
    public List<BizUserAddress> getUserAddresses(Long userId) {
        LambdaQueryWrapper<BizUserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizUserAddress::getUserId, userId);
        return bizUserAddressMapper.selectList(queryWrapper);
    }

    @Override
    public void addUserAddress(Long userId, BizUserAddress address) {
        address.setUserId(userId);
        // 如果是默认地址，将其他地址设为非默认
        if (address.getIsDefault() == 1) {
            LambdaQueryWrapper<BizUserAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BizUserAddress::getUserId, userId);
            queryWrapper.eq(BizUserAddress::getIsDefault, 1);
            BizUserAddress defaultAddress = bizUserAddressMapper.selectOne(queryWrapper);
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(0);
                bizUserAddressMapper.updateById(defaultAddress);
            }
        }
        bizUserAddressMapper.insert(address);
    }

    @Override
    public void updateUserAddress(Long userId, Long addressId, BizUserAddress address) {
        // 检查地址是否属于该用户
        BizUserAddress existingAddress = bizUserAddressMapper.selectById(addressId);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限修改");
        }

        address.setId(addressId);
        address.setUserId(userId);
        // 如果是默认地址，将其他地址设为非默认
        if (address.getIsDefault() == 1) {
            LambdaQueryWrapper<BizUserAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BizUserAddress::getUserId, userId);
            queryWrapper.eq(BizUserAddress::getIsDefault, 1);
            queryWrapper.ne(BizUserAddress::getId, addressId);
            BizUserAddress defaultAddress = bizUserAddressMapper.selectOne(queryWrapper);
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(0);
                bizUserAddressMapper.updateById(defaultAddress);
            }
        }
        bizUserAddressMapper.updateById(address);
    }

    @Override
    public void deleteUserAddress(Long userId, Long addressId) {
        // 检查地址是否属于该用户
        BizUserAddress existingAddress = bizUserAddressMapper.selectById(addressId);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限删除");
        }

        bizUserAddressMapper.deleteById(addressId);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    private String resolveRole(SysUser sysUser) {
        if (sysUser.getRole() != null && !sysUser.getRole().trim().isEmpty()) {
            String dbRole = sysUser.getRole().trim().toUpperCase();
            if (!"USER".equals(dbRole)) {
                return dbRole;
            }
        }
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
