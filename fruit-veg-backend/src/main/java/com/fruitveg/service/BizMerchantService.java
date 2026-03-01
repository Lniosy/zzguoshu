package com.fruitveg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitveg.entity.BizMerchant;

/**
 * 商家表Service接口
 *
 * @author lniosy
 * @since 2026-02-27
 */
public interface BizMerchantService extends IService<BizMerchant> {

    /**
     * 根据用户ID查询商家信息
     *
     * @param userId 用户ID
     * @return 商家信息
     */
    BizMerchant getByUserId(Long userId);

    /**
     * 申请商家入驻
     *
     * @param merchant 商家信息
     * @return 是否成功
     */
    boolean applyForMerchant(BizMerchant merchant);

    /**
     * 审核商家入驻申请
     *
     * @param id 商家ID
     * @param status 审核状态：1通过 2拒绝
     * @return 是否成功
     */
    boolean auditMerchant(Long id, Integer status);
}
