package com.ticketstorm.initialize.config;

import com.ticketstorm.initialize.execute.ApplicationCommandLineRunnerExecute;
import com.ticketstorm.initialize.execute.ApplicationInitializingBeanExecute;
import com.ticketstorm.initialize.execute.ApplicationPostConstructExecute;
import com.ticketstorm.initialize.execute.ApplicationStartEventListenerExecute;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;


public class InitializeAutoConfig {
    
    @Bean
    public ApplicationInitializingBeanExecute applicationInitializingBeanExecute(
            ConfigurableApplicationContext applicationContext){
        return new ApplicationInitializingBeanExecute(applicationContext);
    }
    
    @Bean
    public ApplicationPostConstructExecute applicationPostConstructExecute(
            ConfigurableApplicationContext applicationContext){
        return new ApplicationPostConstructExecute(applicationContext);
    }
    
    @Bean
    public ApplicationStartEventListenerExecute applicationStartEventListenerExecute(
            ConfigurableApplicationContext applicationContext){
        return new ApplicationStartEventListenerExecute(applicationContext);
    }
    
    @Bean
    public ApplicationCommandLineRunnerExecute applicationCommandLineRunnerExecute(
            ConfigurableApplicationContext applicationContext){
        return new ApplicationCommandLineRunnerExecute(applicationContext);
    }
}
