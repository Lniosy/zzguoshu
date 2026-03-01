package com.fruitveg.security;

import com.fruitveg.entity.SysUser;
import com.fruitveg.entity.BizMerchant;
import com.fruitveg.service.SysUserService;
import com.fruitveg.service.BizMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private BizMerchantService bizMerchantService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            // 尝试根据手机号查询
            user = sysUserService.getByPhone(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 获取用户角色
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 默认用户角色
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 检查是否是商家
        BizMerchant merchant = bizMerchantService.getByUserId(user.getId());
        if (merchant != null && merchant.getStatus() == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MERCHANT"));
        }

        // 检查是否是管理员（手机号为13800138000的用户）
        if ("13800138000".equals(user.getPhone()) || "admin".equals(user.getUsername())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // 返回用户详情
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true,
                true,
                user.getStatus() == 1,
                authorities
        );
    }
}
