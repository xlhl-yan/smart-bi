package com.yupi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 队列测试，仅在开发环境和测试环境生效
 *
 * @author xlhl
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev", "test"})
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name) {
        log.info(name);
        CompletableFuture.runAsync(() -> {
            log.info(Thread.currentThread().getName() + " 任务执行中 params=>" + name);
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get() {
        HashMap<String, Object> map = new HashMap<>(16);
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);

        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);

        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已经成功完成的任务数", completedTaskCount);

        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数", activeCount);

        return JSONUtil.toJsonStr(map);
    }
}
