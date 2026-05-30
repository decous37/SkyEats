package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import com.sky.entity.SetmealDish;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "用户端套餐相关接口")
@Slf4j
public class SetmealController {

    private final SetmealService setmealService;
    private final RedisTemplate redisTemplate;

    public SetmealController(SetmealService setmealService, RedisTemplate redisTemplate) {


        this.setmealService = setmealService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("用户端根据分类id查询套餐：{}", categoryId);

        String key = "setmeal_" + categoryId;

        //1. 先从 Redis 查询套餐缓存
        List<Setmeal> list = (List<Setmeal>) redisTemplate.opsForValue().get(key);

        //2. 如果缓存存在，直接返回
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        //3. 如果缓存不存在，查询数据库
        list = setmealService.list(categoryId);

        //4. 将查询结果写入 Redis
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<SetmealDish>> dishList(@PathVariable Long id) {
        log.info("根据套餐id查询包含的菜品：{}", id);
        List<SetmealDish> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }
}