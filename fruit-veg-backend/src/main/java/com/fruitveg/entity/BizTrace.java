package com.fruitveg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 溯源信息表
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Data
@TableName("biz_trace")
public class BizTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 产地名称
     */
    @TableField("origin_name")
    private String originName;

    /**
     * 产地地址
     */
    @TableField("origin_address")
    private String originAddress;

    /**
     * 经度
     */
    @TableField("origin_longitude")
    private BigDecimal originLongitude;

    /**
     * 纬度
     */
    @TableField("origin_latitude")
    private BigDecimal originLatitude;

    /**
     * 种植方式
     */
    @TableField("plant_method")
    private String plantMethod;

    /**
     * 种植时间
     */
    @TableField("plant_time")
    private Date plantTime;

    /**
     * 采收时间
     */
    @TableField("harvest_time")
    private Date harvestTime;

    /**
     * 施肥记录JSON
     */
    @TableField("fertilizer_record")
    private String fertilizerRecord;

    /**
     * 农药记录JSON
     */
    @TableField("pesticide_record")
    private String pesticideRecord;

    /**
     * 检测报告URL
     */
    @TableField("test_report")
    private String testReport;

    /**
     * 储存条件
     */
    @TableField("storage_condition")
    private String storageCondition;

    /**
     * 保质期（天）
     */
    @TableField("shelf_life")
    private Integer shelfLife;

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
