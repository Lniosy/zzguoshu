package com.fruitveg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitveg.entity.BizCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类表Mapper接口
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Mapper
public interface BizCategoryMapper extends BaseMapper<BizCategory> {

    /**
     * 查询所有启用的分类
     *
     * @return 分类列表
     */
    java.util.List<BizCategory> selectEnabledCategories();
}
