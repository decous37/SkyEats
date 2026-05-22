package com.sky.mapper;

import com.sky.dto.SetmealDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import com.sky.entity.Setmeal;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类 id 查询套餐数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增套餐
     * @param setmeal
     */
    @Insert("insert into setmeal (category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})"
    )
    @Options(useGeneratedKeys = true,keyProperty = "id")
    void insert(Setmeal setmeal);
}