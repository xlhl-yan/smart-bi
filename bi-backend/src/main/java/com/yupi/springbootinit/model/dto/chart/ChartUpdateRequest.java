package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author xlhl
 */
@Data
public class ChartUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图标原始信息
     */
    private String chartData;

    /**
     * 图标信息
     */
    private String charType;


    private static final long serialVersionUID = 1L;
}