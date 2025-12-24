package com.ticketstorm.initialize.base;

import org.springframework.beans.factory.InitializingBean;

import static com.ticketstorm.initialize.constant.InitializeHandlerType.APPLICATION_INITIALIZING_BEAN;


public abstract class AbstractApplicationInitializingBeanHandler implements InitializeHandler {
    
    @Override
    public String type() {
        return APPLICATION_INITIALIZING_BEAN;
    }
}
