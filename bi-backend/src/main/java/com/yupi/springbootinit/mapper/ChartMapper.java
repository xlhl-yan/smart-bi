package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Chart;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xlhl
 * @description 针对表【chart】的数据库操作Mapper
 * @createDate 2023-08-25 19:51:35
 * @Entity com.yupi.springbootinit.model.entity.Chart
 */
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * 测试动态 sql
     *
     * @param sql
     * @return
     */
    List<Map<String, Object>> queryChartData(@Param("querySql") String sql);
}




