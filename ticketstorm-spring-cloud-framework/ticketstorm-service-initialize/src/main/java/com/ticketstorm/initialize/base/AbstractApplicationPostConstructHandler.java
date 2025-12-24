package com.ticketstorm.initialize.base;

import jakarta.annotation.PostConstruct;

import static com.ticketstorm.initialize.constant.InitializeHandlerType.APPLICATION_POST_CONSTRUCT;


public abstract class AbstractApplicationPostConstructHandler implements InitializeHandler {
    
    @Override
    public String type() {
        return APPLICATION_POST_CONSTRUCT;
    }
}
