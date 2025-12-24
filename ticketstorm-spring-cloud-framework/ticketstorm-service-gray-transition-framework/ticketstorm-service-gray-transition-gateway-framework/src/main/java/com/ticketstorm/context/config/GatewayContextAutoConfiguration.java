package com.ticketstorm.context.config;

import com.ticketstorm.context.ContextHandler;
import com.ticketstorm.context.filter.GatewayWorkClearFilter;
import com.ticketstorm.context.filter.GatewayWorkRouteFilter;
import com.ticketstorm.context.impl.GatewayContextHandler;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;


public class GatewayContextAutoConfiguration {
    
    @Bean
    public GlobalFilter gatewayWorkRouteFilter() {
        return new GatewayWorkRouteFilter();
    }
    
    @Bean
    public GlobalFilter gatewayWorkClearFilter() {
        return new GatewayWorkClearFilter();
    }
    
    @Bean
    public ContextHandler webMvcContext(){
        return new GatewayContextHandler();
    }
}
