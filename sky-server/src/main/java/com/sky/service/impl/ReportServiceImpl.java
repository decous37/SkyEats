package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //1. 准备日期列表和营业额列表
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();

        //2. 从 begin 循环到 end，每天查一次营业额
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            dateList.add(date);

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Double turnover = orderMapper.sumByStatusAndOrderTime(
                    Orders.COMPLETED,
                    beginTime,
                    endTime
            );

            turnoverList.add(turnover == null ? 0.0 : turnover);

            date = date.plusDays(1);
        }

        //3. 组装成前端图表需要的逗号分隔字符串
        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList.stream().map(LocalDate::toString).toList()))
                .turnoverList(String.join(",", turnoverList.stream().map(String::valueOf).toList()))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //1. 准备日期列表、用户总量列表、新增用户列表
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        //2. 从 begin 循环到 end，每天统计一次
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            dateList.add(date);

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //当天新增用户数
            Integer newUser = userMapper.countByCreateTime(beginTime, endTime);

            //截止当天结束的用户总数
            Integer totalUser = userMapper.countByCreateTime(null, endTime);

            newUserList.add(newUser == null ? 0 : newUser);
            totalUserList.add(totalUser == null ? 0 : totalUser);

            date = date.plusDays(1);
        }

        //3. 组装返回结果
        return UserReportVO.builder()
                .dateList(String.join(",", dateList.stream().map(LocalDate::toString).toList()))
                .totalUserList(String.join(",", totalUserList.stream().map(String::valueOf).toList()))
                .newUserList(String.join(",", newUserList.stream().map(String::valueOf).toList()))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //1. 准备日期列表、订单总数列表、有效订单数列表
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        //2. 从 begin 循环到 end，每天统计一次
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            dateList.add(date);

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //当天全部订单数
            Integer orderCount = orderMapper.countByOrderTime(beginTime, endTime);

            //当天有效订单数：已完成订单
            Integer validOrderCount = orderMapper.countByStatusAndOrderTime(
                    Orders.COMPLETED,
                    beginTime,
                    endTime
            );

            orderCountList.add(orderCount == null ? 0 : orderCount);
            validOrderCountList.add(validOrderCount == null ? 0 : validOrderCount);

            date = date.plusDays(1);
        }

        //3. 统计整个时间范围内的总订单数和有效订单数
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        Integer totalOrderCount = orderMapper.countByOrderTime(beginTime, endTime);
        Integer validOrderCount = orderMapper.countByStatusAndOrderTime(
                Orders.COMPLETED,
                beginTime,
                endTime
        );

        //4. 计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != null && totalOrderCount > 0 && validOrderCount != null) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        //5. 组装返回结果
        return OrderReportVO.builder()
                .dateList(String.join(",", dateList.stream().map(LocalDate::toString).toList()))
                .orderCountList(String.join(",", orderCountList.stream().map(String::valueOf).toList()))
                .validOrderCountList(String.join(",", validOrderCountList.stream().map(String::valueOf).toList()))
                .totalOrderCount(totalOrderCount == null ? 0 : totalOrderCount)
                .validOrderCount(validOrderCount == null ? 0 : validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //1. 构造查询时间范围
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //2. 查询销量排名前10
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        //3. 分别取出商品名称和销量
        List<String> nameList = salesTop10.stream()
                .map(GoodsSalesDTO::getName)
                .toList();

        List<String> numberList = salesTop10.stream()
                .map(goodsSalesDTO -> String.valueOf(goodsSalesDTO.getNumber()))
                .toList();

        //4. 组装返回结果
        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        try {
            //1. 创建 Excel 工作簿和工作表
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("运营数据报表");

            //2. 写入标题和表头
            sheet.createRow(0).createCell(0).setCellValue("运营数据报表");
            sheet.createRow(2).createCell(0).setCellValue("日期");
            sheet.getRow(2).createCell(1).setCellValue("营业额");
            sheet.getRow(2).createCell(2).setCellValue("有效订单数");
            sheet.getRow(2).createCell(3).setCellValue("订单完成率");
            sheet.getRow(2).createCell(4).setCellValue("平均客单价");
            sheet.getRow(2).createCell(5).setCellValue("新增用户数");

            //3. 计算最近30天日期范围
            LocalDate begin = LocalDate.now().minusDays(30);
            LocalDate end = LocalDate.now().minusDays(1);

            int rowIndex = 3;
            LocalDate date = begin;

            //4. 按天写入运营数据
            while (!date.isAfter(end)) {
                LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

                BusinessDataVO businessDataVO = getBusinessData(beginTime, endTime);

                sheet.createRow(rowIndex).createCell(0).setCellValue(date.toString());
                sheet.getRow(rowIndex).createCell(1).setCellValue(businessDataVO.getTurnover());
                sheet.getRow(rowIndex).createCell(2).setCellValue(businessDataVO.getValidOrderCount());
                sheet.getRow(rowIndex).createCell(3).setCellValue(businessDataVO.getOrderCompletionRate());
                sheet.getRow(rowIndex).createCell(4).setCellValue(businessDataVO.getUnitPrice());
                sheet.getRow(rowIndex).createCell(5).setCellValue(businessDataVO.getNewUsers());

                rowIndex++;
                date = date.plusDays(1);
            }

            //5. 设置响应头，告诉浏览器这是一个 Excel 文件
            String fileName = URLEncoder.encode("运营数据报表.xlsx", "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            //6. 把 Excel 写入响应流
            workbook.write(response.getOutputStream());

            //7. 关闭资源
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        //1. 查询营业额
        Double turnover = orderMapper.sumByStatusAndOrderTime(Orders.COMPLETED, begin, end);

        //2. 查询有效订单数
        Integer validOrderCount = orderMapper.countByStatusAndOrderTime(Orders.COMPLETED, begin, end);

        //3. 查询总订单数
        Integer totalOrderCount = orderMapper.countByOrderTime(begin, end);

        //4. 查询新增用户数
        Integer newUsers = userMapper.countByCreateTime(begin, end);

        //5. 计算订单完成率和平均客单价
        Double orderCompletionRate = 0.0;
        Double unitPrice = 0.0;

        if (totalOrderCount != null && totalOrderCount > 0 && validOrderCount != null) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        if (validOrderCount != null && validOrderCount > 0 && turnover != null) {
            unitPrice = turnover / validOrderCount;
        }

        //6. 返回运营数据
        return BusinessDataVO.builder()
                .turnover(turnover == null ? 0.0 : turnover)
                .validOrderCount(validOrderCount == null ? 0 : validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers == null ? 0 : newUsers)
                .build();
    }
}