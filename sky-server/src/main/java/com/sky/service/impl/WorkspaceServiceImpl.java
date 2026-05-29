package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public WorkspaceServiceImpl(OrderMapper orderMapper,
                                UserMapper userMapper,
                                DishMapper dishMapper,
                                SetmealMapper setmealMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    public BusinessDataVO getBusinessData() {
        //1. 获取今天的开始时间和结束时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        //2. 查询今日营业额
        Double turnover = orderMapper.sumByStatusAndOrderTime(5, begin, end);

        //3. 查询今日有效订单数
        Integer validOrderCount = orderMapper.countByStatusAndOrderTime(5, begin, end);

        //4. 查询今日总订单数
        Integer totalOrderCount = orderMapper.countByOrderTime(begin, end);

        //5. 查询今日新增用户数
        Integer newUsers = userMapper.countByCreateTime(begin, end);

        //6. 计算订单完成率和平均客单价
        Double orderCompletionRate = 0.0;
        Double unitPrice = 0.0;

        if (totalOrderCount != null && totalOrderCount > 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        if (validOrderCount != null && validOrderCount > 0 && turnover != null) {
            unitPrice = turnover / validOrderCount;
        }

        //7. 组装返回结果
        return BusinessDataVO.builder()
                .turnover(turnover == null ? 0.0 : turnover)
                .validOrderCount(validOrderCount == null ? 0 : validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers == null ? 0 : newUsers)
                .build();
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        //1. 待接单数量
        Integer waitingOrders = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);

        //2. 待派送数量
        Integer deliveredOrders = orderMapper.countByStatus(Orders.CONFIRMED);

        //3. 已完成数量
        Integer completedOrders = orderMapper.countByStatus(Orders.COMPLETED);

        //4. 已取消数量
        Integer cancelledOrders = orderMapper.countByStatus(Orders.CANCELLED);

        //5. 全部订单数量
        Integer allOrders = orderMapper.countByOrderTime(null, null);

        //6. 组装返回
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    @Override
    public DishOverViewVO getDishOverView() {
        //1. 查询已启售菜品数量
        Integer sold = dishMapper.countByStatus(StatusConstant.ENABLE);

        //2. 查询已停售菜品数量
        Integer discontinued = dishMapper.countByStatus(StatusConstant.DISABLE);

        //3. 组装返回结果
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        //1. 查询已启售套餐数量
        Integer sold = setmealMapper.countByStatus(StatusConstant.ENABLE);

        //2. 查询已停售套餐数量
        Integer discontinued = setmealMapper.countByStatus(StatusConstant.DISABLE);

        //3. 组装返回结果
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}