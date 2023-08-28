package com.yupi.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ChartMapperTest {

    @Resource
    private ChartMapper chartMapper;

    @Test
    void queryChartData() {
        String querySql = String.format("select * from chart_%s", 1695978462034010113L);
        List<Map<String, Object>> chartData = chartMapper.queryChartData(querySql);

        System.out.println("chartData = " + chartData);
    }
}