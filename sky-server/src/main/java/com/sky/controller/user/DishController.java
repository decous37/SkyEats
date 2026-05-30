package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "用户端菜品相关接口")
@Slf4j
public class DishController {

    private final DishService dishService;
    private final RedisTemplate redisTemplate;

    public DishController(DishService dishService, RedisTemplate redisTemplate) {
        this.dishService = dishService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("用户端根据分类id查询菜品：{}", categoryId);

        String key = "dish_" + categoryId;

        //1. 先从 Redis 查询缓存数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);

        //2. 如果 Redis 中存在，直接返回
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        //3. 如果 Redis 中不存在，查询数据库
        list = dishService.listWithFlavor(categoryId);

        //4. 将查询结果写入 Redis
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }
}