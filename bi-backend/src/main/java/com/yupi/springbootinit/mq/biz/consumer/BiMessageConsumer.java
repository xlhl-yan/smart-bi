package com.yupi.springbootinit.mq.biz.consumer;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.RabbitmqConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.enums.ChartStatusEnum;
import com.yupi.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MyMessageConsumer
 *
 * @author xlhl
 * @version 1.0
 * @description Consumer 消费者
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 程序监听的消息队列和确认机制
     *
     * @param message
     * @param channel
     * @param deliveryTag
     * @throws Exception
     */
    @RabbitListener(queues = RabbitmqConstant.BI_QUEUE_NAME, ackMode = "MANUAL")
    public void onMessage(String message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {

        if (StringUtils.isBlank(message)) {
            //  消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        Long charId = Long.parseLong(message);
        Chart chart = chartService.getById(charId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表信息为空");
        }
        //  设置图表状态为执行中
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        ChartStatusEnum inProcessOf = ChartStatusEnum.IN_PROCESS_OF;
        updateChart.setStatus(inProcessOf.getCode());
        boolean flag = chartService.updateById(updateChart);
        //  无法设置为执行中则设置为失败
        if (!flag) {
            log.error("修改图表任务状态{}时失败", inProcessOf.getDesc());
            ChartStatusEnum fail = ChartStatusEnum.FAIL;
            updateChart.setStatus(fail.getCode());
            boolean updateFail = chartService.updateById(chart);
            if (!updateFail) {
                handlerChartUpdateErrorLog(chart.getId(), String.format("修改图表任务状态为%s时失败", fail.getDesc()));
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法修改任务状态");
            }
        }
        String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, userInput(chart));
        log.info("AI 生成结果为：" + result);
        String[] split = result.split("【【【【【");
        if (split.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            handlerChartUpdateErrorLog(chart.getId(), "AI 生成错误");
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();
        updateChart.setGenChart(genChart);
        updateChart.setGenResult(genResult);
        ChartStatusEnum success = ChartStatusEnum.SUCCESS;
        updateChart.setStatus(success.getCode());
        boolean updateResult = chartService.updateById(updateChart);

        redisTemplate.delete(CommonConstant.REDISSON_KEY + chart.getId());

        if (!updateResult) {
            channel.basicNack(deliveryTag, false, false);
            handlerChartUpdateErrorLog(chart.getId(), String.format("修改图表任务状态为%s时失败", success.getDesc()));
        }
        //  确认消息
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 获取用户的输入信息
     *
     * @param chart
     * @return
     */
    private String userInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csv = chart.getChartData();

        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal = ",请使用" + chartType;
        }
        //  调用 AI 接口需要的数据
        return "分析需求:" + "\n" +
                userGoal +
                "原始数据:" + "\n" +
                csv + "\n";
    }

    /**
     * 修改图表任务状态失败处理
     *
     * @param chartId
     * @param cnMessage
     */
    private void handlerChartUpdateErrorLog(Long chartId, String cnMessage) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setExecMessage(cnMessage);
        chart.setStatus(ChartStatusEnum.FAIL.getCode());

        boolean update = chartService.updateById(chart);
        if (!update) {
            log.error(cnMessage);
        }
    }
}
