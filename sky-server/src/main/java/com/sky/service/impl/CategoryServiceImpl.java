package com.sky.service.impl;
import com.sky.constant.MessageConstant;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import java.util.List;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.mapper.SetmealMapper;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, DishMapper dishMapper,  SetmealMapper setmealMapper) {
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }
    @Override

    public void save(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.DISABLE);

        LocalDateTime now=LocalDateTime.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);

        Long currentId=BaseContext.getCurrentId();
        category.setCreateUser(currentId);
        category.setUpdateUser(currentId);

        categoryMapper.insert(category);

    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        return  new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void deleteById(Long id) {
        Integer dishCount = dishMapper.countByCategoryId(id);
        if(dishCount>0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);

        }

        Integer setmealCount = setmealMapper.countByCategoryId(id);
        if(setmealCount>0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category=new Category();

        BeanUtils.copyProperties(categoryDTO,category);

        LocalDateTime now=LocalDateTime.now();
        category.setUpdateTime(now);

        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category= Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        categoryMapper.update(category);
    }

    @Override
    public List<Category> list(Integer type) {
        Category category = Category.builder()
                .type(type)
                .status(StatusConstant.ENABLE)
                .build();

        return categoryMapper.list(category);
    }
}
