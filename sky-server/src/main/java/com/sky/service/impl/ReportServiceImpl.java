package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
}