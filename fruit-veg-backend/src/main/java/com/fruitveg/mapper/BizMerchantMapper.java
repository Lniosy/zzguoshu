package com.fruitveg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitveg.entity.BizMerchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家表Mapper接口
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Mapper
public interface BizMerchantMapper extends BaseMapper<BizMerchant> {

    /**
     * 根据用户ID查询商家信息
     *
     * @param userId 用户ID
     * @return 商家信息
     */
    BizMerchant selectByUserId(Long userId);
}
