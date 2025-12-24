package com.ticketstorm.initialize.execute;

import com.ticketstorm.initialize.execute.base.AbstractApplicationExecute;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.annotation.PostConstruct;

import static com.ticketstorm.initialize.constant.InitializeHandlerType.APPLICATION_POST_CONSTRUCT;


public class ApplicationPostConstructExecute extends AbstractApplicationExecute {
    
    public ApplicationPostConstructExecute(ConfigurableApplicationContext applicationContext){
        super(applicationContext);
    }
    
    @PostConstruct
    public void postConstructExecute() {
        execute();
    }
    
    @Override
    public String type() {
        return APPLICATION_POST_CONSTRUCT;
    }
}
