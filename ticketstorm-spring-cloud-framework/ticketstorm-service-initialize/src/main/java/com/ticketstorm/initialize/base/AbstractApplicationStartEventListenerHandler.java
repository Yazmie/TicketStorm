package com.ticketstorm.initialize.base;

import org.springframework.beans.factory.InitializingBean;

import static com.ticketstorm.initialize.constant.InitializeHandlerType.APPLICATION_EVENT_LISTENER;


public abstract class AbstractApplicationStartEventListenerHandler implements InitializeHandler {
    
    @Override
    public String type() {
        return APPLICATION_EVENT_LISTENER;
    }
}
