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
    
    @Autowired
    private RedisCache redisCache;
    
    public String parseToken(String token,String tokenSecret){
        String userStr = TokenUtil.parseToken(token,tokenSecret);
        if (StringUtil.isNotEmpty(userStr)) {
            return JSONObject.parseObject(userStr).getString("userId");
        }
        return null;
    }
    
    public UserVo getUser(String token,String code,String tokenSecret){
        UserVo userVo = null;
        String userId = parseToken(token,tokenSecret);
        if (StringUtil.isNotEmpty(userId)) {
            userVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, userId), UserVo.class);
        }
        return Optional.ofNullable(userVo).orElseThrow(() -> new TicketStormFrameException(BaseCode.LOGIN_USER_NOT_EXIST));
    }
}
