package com.ticketstorm.context.config;

import com.ticketstorm.context.ContextHandler;
import com.ticketstorm.context.impl.WebMvcContextHandler;
import org.springframework.context.annotation.Bean;


public class WebMvcContextAutoConfiguration {
    
    @Bean
    public ContextHandler webMvcContext(){
        return new WebMvcContextHandler();
    }
}
