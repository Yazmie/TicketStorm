package com.ticketstorm.config;

import com.ticketstorm.properties.AjCaptchaProperties;
import com.ticketstorm.captcha.service.CaptchaCacheService;
import com.ticketstorm.captcha.service.CaptchaService;
import com.ticketstorm.captcha.service.impl.CaptchaServiceFactory;
import com.ticketstorm.service.CaptchaCacheServiceRedisImpl;
import com.ticketstorm.service.CaptchaHandle;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;


public class CaptchaAutoConfig {
    
    @Bean
    @Lazy
    public CaptchaHandle captchaHandle(CaptchaService captchaService){
        return new CaptchaHandle(captchaService);
    }
    
    @Bean(name = "AjCaptchaCacheService")
    @Primary
    public CaptchaCacheService captchaCacheService(AjCaptchaProperties config, StringRedisTemplate redisTemplate){
        //缓存类型redis/local/....
        CaptchaCacheService ret = CaptchaServiceFactory.getCache(config.getCacheType().name());
        if(ret instanceof CaptchaCacheServiceRedisImpl){
            ((CaptchaCacheServiceRedisImpl)ret).setStringRedisTemplate(redisTemplate);
        }
        return ret;
    }
}
