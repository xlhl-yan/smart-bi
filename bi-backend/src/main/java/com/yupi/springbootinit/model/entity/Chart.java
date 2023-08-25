package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图标信息表
 *
 * @author xlhl
 * @TableName chart
 */
@TableName(value = "chart")
@Data
public class Chart implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分析目标
     */
    @TableField(value = "goal")
    private String goal;

    /**
     * 图标原始信息
     */
    @TableField(value = "chartData")
    private String chartData;

    /**
     * 创建人id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 图标信息
     */
    @TableField(value = "charType")
    private String charType;

    /**
     * AI 生成图表信息
     */
    @TableField(value = "genChart")
    private String genChart;

    /**
     * AI 生成分析结论
     */
    @TableField(value = "genResult")
    private String genResult;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}