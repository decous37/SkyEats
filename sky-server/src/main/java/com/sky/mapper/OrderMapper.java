package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     * @return
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 根据id动态修改订单
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据订单状态统计数量
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 根据状态和下单时间统计营业额
     * @param status
     * @param begin
     * @param end
     * @return
     */
    Double sumByStatusAndOrderTime(@Param("status") Integer status,
                                   @Param("begin") LocalDateTime begin,
                                   @Param("end") LocalDateTime end);

    /**
     * 根据状态和下单时间统计订单数量
     * @param status
     * @param begin
     * @param end
     * @return
     */
    Integer countByStatusAndOrderTime(@Param("status") Integer status,
                                      @Param("begin") LocalDateTime begin,
                                      @Param("end") LocalDateTime end);

    /**
     * 根据下单时间统计订单数量
     * @param begin
     * @param end
     * @return
     */
    Integer countByOrderTime(@Param("begin") LocalDateTime begin,
                             @Param("end") LocalDateTime end);

    /**
     * 查询销量排名Top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin,
                                      @Param("end") LocalDateTime end);
}