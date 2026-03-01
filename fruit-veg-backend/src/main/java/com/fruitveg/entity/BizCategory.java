package com.fruitveg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品分类表
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Data
@TableName("biz_category")
public class BizCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父分类ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态：0禁用 1正常
     */
    @TableField("status")
    private Integer status;

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
