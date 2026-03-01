package com.fruitveg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单表
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Data
@TableName("biz_order")
public class BizOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @TableField("pay_amount")
    private BigDecimal payAmount;

    /**
     * 支付方式
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private Date payTime;

    /**
     * 订单状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 收货人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 物流公司
     */
    @TableField("logistics_company")
    private String logisticsCompany;

    /**
     * 物流单号
     */
    @TableField("logistics_no")
    private String logisticsNo;

    /**
     * 发货时间
     */
    @TableField("ship_time")
    private Date shipTime;

    /**
     * 收货时间
     */
    @TableField("receive_time")
    private Date receiveTime;

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
