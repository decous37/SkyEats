package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
//import springfox.documentation.annotations.Cacheable;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "用户端分类相关接口")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    @ApiOperation("查询分类")
    @Cacheable(cacheNames = "categoryCache", key = "#p0 == null ? 'all' : #p0")
    public Result<List<Category>> list(Integer type) {
        log.info("用户端查询分类：{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}