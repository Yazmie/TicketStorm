package com.ticketstorm.feign;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static com.ticketstorm.constant.Constant.SERVER_GRAY;




public class ExtraFeignAutoConfiguration {
    
    @Value(SERVER_GRAY)
    public String serverGray;
    
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor(){
        return new FeignRequestInterceptor(serverGray);
    }
}
