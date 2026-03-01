package com.fruitveg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品表
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Data
@TableName("biz_product")
public class BizProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 主图URL
     */
    @TableField("main_image")
    private String mainImage;

    /**
     * 详情图片JSON
     */
    @TableField("images")
    private String images;

    /**
     * 销售价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 商品详情
     */
    @TableField("description")
    private String description;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 状态：0下架 1上架 2审核中
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标志：0未删除 1已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
