package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * BiResponse
 *
 * @author xlhl
 * @version 1.0
 * @description AI返回的图表代码和分析结论响应体
 */
@Data
public class BiResponse {

    /**
     * 图表代码
     */
    private String genChart;
    /**
     * 分析结果
     */
    private String genResult;

    /**
     * 图表id
     */
    private Long chartId;
}
