package com.ticketstorm.initialize.base;

import org.springframework.boot.CommandLineRunner;

import static com.ticketstorm.initialize.constant.InitializeHandlerType.APPLICATION_COMMAND_LINE_RUNNER;
import static com.ticketstorm.initialize.constant.InitializeHandlerType.APPLICATION_POST_CONSTRUCT;


public abstract class AbstractApplicationCommandLineRunnerHandler implements InitializeHandler {
    
    @Override
    public String type() {
        return APPLICATION_COMMAND_LINE_RUNNER;
    }
}
