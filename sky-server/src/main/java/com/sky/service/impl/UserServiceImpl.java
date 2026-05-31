package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final String MOCK_OPENID = "mock_openid_dev";

    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final WeChatProperties weChatProperties;

    public UserServiceImpl(UserMapper userMapper, JwtProperties jwtProperties, WeChatProperties weChatProperties) {
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
        this.weChatProperties = weChatProperties;
    }

    @Override
    public UserLoginVO wxLogin(UserLoginDTO userLoginDTO) {
        //1. 本地模拟微信登录，先固定一个 openid
        String openid;

        if ("real".equals(weChatProperties.getLoginMode())) {
            // 后续接入微信 code2session
            openid = getOpenid(userLoginDTO.getCode());
        } else {
            // 本地模拟微信登录
            openid = MOCK_OPENID;
        }
        //2. 根据 openid 查询用户
        User user = userMapper.getByOpenid(openid);

        //3. 如果用户不存在，则自动注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //4. 生成 JWT 令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        //5. 返回给小程序
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
    }

    private String getOpenid(String code) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", weChatProperties.getAppid())
                .queryParam("secret", weChatProperties.getSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        String json = new RestTemplate().getForObject(url, String.class);
        JSONObject jsonObject = JSON.parseObject(json);

        String openid = jsonObject.getString("openid");

        if (openid == null || openid.length() == 0) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        return openid;
    }
}