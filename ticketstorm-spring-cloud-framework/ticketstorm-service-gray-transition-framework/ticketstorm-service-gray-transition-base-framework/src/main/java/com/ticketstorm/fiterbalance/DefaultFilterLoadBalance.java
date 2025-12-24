package com.ticketstorm.fiterbalance;


import com.ticketstorm.balance.FilterLoadBalance;
import com.ticketstorm.filter.AbstractServerFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;


@AllArgsConstructor
public class DefaultFilterLoadBalance implements FilterLoadBalance {

    protected final List<AbstractServerFilter> strategyFilterList;

    @Override
    public void selectServer(List<ServiceInstance> servers) {
        for (AbstractServerFilter strategyEnabledFilter : strategyFilterList) {
            strategyEnabledFilter.filter(servers);
        }
    }
}