package com.fruitveg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitveg.entity.BizMerchant;
import com.fruitveg.mapper.BizMerchantMapper;
import com.fruitveg.service.BizMerchantService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 商家表Service实现类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Service
public class BizMerchantServiceImpl extends ServiceImpl<BizMerchantMapper, BizMerchant> implements BizMerchantService {

    @Override
    public BizMerchant getByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public boolean applyForMerchant(BizMerchant merchant) {
        // 设置默认状态为待审核
        merchant.setStatus(0);
        return save(merchant);
    }

    @Override
    public boolean auditMerchant(Long id, Integer status) {
        BizMerchant merchant = getById(id);
        if (merchant != null) {
            merchant.setStatus(status);
            merchant.setAuditTime(new Date());
            return updateById(merchant);
        }
        return false;
    }
}
