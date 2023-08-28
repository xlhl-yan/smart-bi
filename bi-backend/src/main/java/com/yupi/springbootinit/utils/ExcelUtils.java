package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ExcelUtils
 *
 * @author xlhl
 * @version 1.0
 * @description Excel工具类
 */
@Slf4j
public class ExcelUtils {

    /**
     * excel to CSV
     *
     * @param multipartFile excel
     * @return CSV
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("Excel to CSV Error", e);
            e.printStackTrace();
        }
        //  转换为 CSV
        //  读取表头
        if (CollUtil.isEmpty(list)) {
            return "";
        }
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap<Integer, String>) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        StringBuilder csv = new StringBuilder();
        csv.append(StringUtils.join(headerList, ",")).append("\n");
        //  读取数据
        List<String> dataList;
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap<Integer, String>) list.get(i);
            dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            csv.append(StringUtils.join(dataList, ",")).append("\n");
        }

        return csv.toString();
    }

    public static void main(String[] args) {
        try {
            File file = ResourceUtils.getFile("classpath:测试数据.xlsx");
            System.out.println(excelToCsv((MultipartFile) file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
