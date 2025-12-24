package com.ticketstorm.balance.config;

import com.ticketstorm.context.ContextHandler;
import com.ticketstorm.enhance.config.EnhanceLoadBalancerClientConfiguration;
import com.ticketstorm.enhance.config.EnhanceLoadBalancerClientConfiguration.BlockingSupportConfiguration;
import com.ticketstorm.enhance.config.EnhanceLoadBalancerClientConfiguration.ReactiveSupportConfiguration;
import com.ticketstorm.filter.AbstractServerFilter;
import com.ticketstorm.filter.impl.ServerGrayFilter;
import com.ticketstorm.fiterbalance.DefaultFilterLoadBalance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;

import java.util.List;


@LoadBalancerClients(defaultConfiguration = {EnhanceLoadBalancerClientConfiguration.class, ReactiveSupportConfiguration.class, BlockingSupportConfiguration.class})
public class GrayLoadBalanceAutoConfiguration {
    
    @Bean
    public DefaultFilterLoadBalance defaultFilterLoadBalance(List<AbstractServerFilter> strategyEnabledFilterList){
        return new DefaultFilterLoadBalance(strategyEnabledFilterList);
    }
    
    @Bean
    public AbstractServerFilter serverGrayFilter(ContextHandler contextHandler) {
        return new ServerGrayFilter(contextHandler);
    }
}
