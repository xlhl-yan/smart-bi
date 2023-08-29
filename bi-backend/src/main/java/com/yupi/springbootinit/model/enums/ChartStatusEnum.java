package com.yupi.springbootinit.model.enums;

/**
 * 图表任务状态类
 *
 * @author xlhl
 */
public enum ChartStatusEnum {
    /**
     * 成功
     */
    SUCCESS(0, "success"),

    /**
     * 失败
     */
    FAIL(1, "fail"),

    /**
     * 执行中
     */
    IN_PROCESS_OF(2, "in_process_of"),

    /**
     * 尚未开始执行
     */
    NOT_YET(3, "not_yet");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    ChartStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
