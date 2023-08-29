package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.ChartStatusEnum;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表接口
 *
 * @author xlhl
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RedisLimiterManager redisLimiterManager;
    /**
     * 文件大小 1 MB
     */
    private static final long FILE_MAX_SIZE = 1024 * 1024;

    /**
     * 文件后缀名
     */
    private static final List<String> SUFFIX_LIST = Arrays.asList("xlsx", "xls");

    /**
     * 同步分析
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/sync")
    public BaseResponse<BiResponse> genChartByAiSync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        redisLimiterManager.doRateLimit(String.format("genChartByAiSync_%s", loginUser.getId()), 1L);

        //  校验
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isAllBlank(name, goal, chartType), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(name) || name.length() >= 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标不能为空");

        //  校验文件大小
        long filesize = multipartFile.getSize();
        String filename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(filesize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件过大");

        //  文件后缀
        String suffix = FileUtil.getSuffix(filename);
        ThrowUtils.throwIf(!SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型不符合要求");

        //  读取用户上传文件 并压缩为 CSV
        String csv = ExcelUtils.excelToCsv(multipartFile);

        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal = ",请使用" + chartType;
        }
        //  调用 AI 接口需要的数据
        String data = "分析需求:" + "\n" +
                userGoal +
                "原始数据:" + "\n" +
                csv + "\n";

        //  调用 AI 获取分析结果
        //  模型 id
        Long biModelId = 1695768463945039874L;
        String result = aiManager.doChat(biModelId, data);
        log.info("AI 生成结果为：" + result);
        String[] split = result.split("【【【【【");
        for (String s : split) {
            log.info("切割后的结果分别为：{}", s);
        }
        ThrowUtils.throwIf(split.length < 3, ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        String genChart = split[1].trim();
        String genResult = split[2].trim();
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);

        //  插入数据到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csv);
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);

        biResponse.setChartId(chart.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "数据保存失败");

        return ResultUtils.success(biResponse);
    }


    /**
     * 异步分析
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest,
                                                 HttpServletRequest request) {
        //  限流
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        redisLimiterManager.doRateLimit(String.format("genChartByAiAsync_%s", loginUser.getId()), 1L);
        //  校验
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isAllBlank(name, goal, chartType), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(name) || name.length() >= 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标不能为空");
        //  校验文件大小
        long filesize = multipartFile.getSize();
        String filename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(filesize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件过大");
        //  文件后缀
        String suffix = FileUtil.getSuffix(filename);
        ThrowUtils.throwIf(!SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型不符合要求");
        //  读取用户上传文件 并压缩为 CSV
        String csv = ExcelUtils.excelToCsv(multipartFile);
        //  插入数据到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csv);
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setStatus(ChartStatusEnum.NOT_YET.getCode());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "数据保存失败");
        CompletableFuture.runAsync(() -> {
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
                boolean updateFail = chartService.updateById(updateChart);
                if (!updateFail) {
                    handlerChartUpdateErrorLog(chart.getId(), String.format("修改图表任务状态为%s时失败", fail.getDesc()));
                    return;
                }
            }
            //拼接分析目标
            String userGoal = goal;
            if (StringUtils.isNotBlank(chartType)) {
                userGoal = ",请使用" + chartType;
            }
            //  调用 AI 接口需要的数据
            String data = "分析需求:" + "\n" +
                    userGoal +
                    "原始数据:" + "\n" +
                    csv + "\n";
            //  调用 AI 获取分析结果
            //  模型 id
            Long biModelId = 1695768463945039874L;
            String result = aiManager.doChat(biModelId, data);
            log.info("AI 生成结果为：" + result);
            String[] split = result.split("【【【【【");
            if (split.length < 3) {
                handlerChartUpdateErrorLog(chart.getId(), "AI 生成错误");
                return;
            }
            String genChart = split[1].trim();
            String genResult = split[2].trim();
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            ChartStatusEnum success = ChartStatusEnum.SUCCESS;
            updateChart.setStatus(success.getCode());
            boolean updateResult = chartService.updateById(updateChart);
            if (!updateResult) {
                handlerChartUpdateErrorLog(chart.getId(), String.format("修改图表任务状态为%s时失败", success.getDesc()));
            }
        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
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

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(chartQueryRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                this.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // 参数校验
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        Long userId = chartQueryRequest.getUserId();
        String name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();

        // 拼接查询条件
        queryWrapper.eq(id != null && id < 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}
