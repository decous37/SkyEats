package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

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
}