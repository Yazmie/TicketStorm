package com.ticketstorm.service;

import com.alibaba.fastjson.JSONObject;
import com.ticketstorm.core.RedisKeyManage;
import com.ticketstorm.util.StringUtil;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.jwt.TokenUtil;
import com.ticketstorm.redis.RedisCache;
import com.ticketstorm.redis.RedisKeyBuild;
import com.ticketstorm.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;



@Component
public class TokenService {
    
    private static final String TOKEN_SECRET = "CSYZWECHAT";
    
    @Autowired
    private RedisCache redisCache;
    
    public String parseToken(String token){
        String userStr = TokenUtil.parseToken(token,TOKEN_SECRET);
        if (StringUtil.isNotEmpty(userStr)) {
            return JSONObject.parseObject(userStr).getString("userId");
        }
        return null;
    }
    
    public UserVo getUser(String token){
        UserVo userVo = null;
        String userId = parseToken(token);
        if (StringUtil.isNotEmpty(userId)) {
            userVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, userId), UserVo.class);
        }
        return Optional.ofNullable(userVo).orElseThrow(() -> new TicketStormFrameException(BaseCode.USER_EMPTY));
    }
}
