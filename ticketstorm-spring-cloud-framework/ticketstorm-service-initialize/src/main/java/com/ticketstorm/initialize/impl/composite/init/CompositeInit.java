package com.ticketstorm.initialize.impl.composite.init;

import com.ticketstorm.initialize.base.AbstractApplicationStartEventListenerHandler;
import com.ticketstorm.initialize.impl.composite.CompositeContainer;
import lombok.AllArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;


@AllArgsConstructor
public class CompositeInit extends AbstractApplicationStartEventListenerHandler {
    
    private final CompositeContainer compositeContainer;
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
    
    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        compositeContainer.init(context);
    }
}
