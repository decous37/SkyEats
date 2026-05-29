package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户数据
     * @param user
     */
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values " +
            "(#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * 根据创建时间统计用户数量
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(id) from user where create_time >= #{begin} and create_time <= #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin,
                              @Param("end") LocalDateTime end);
}