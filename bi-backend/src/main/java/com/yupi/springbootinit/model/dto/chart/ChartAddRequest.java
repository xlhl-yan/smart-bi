package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @author xlhl
 */
@Data
public class ChartAddRequest implements Serializable {


    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图标原始信息
     */
    private String chartData;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 图标信息
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}