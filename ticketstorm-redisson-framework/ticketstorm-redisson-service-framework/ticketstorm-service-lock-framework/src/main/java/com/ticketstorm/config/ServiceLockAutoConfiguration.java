package com.ticketstorm.config;

import com.ticketstorm.constant.LockInfoType;
import com.ticketstorm.core.ManageLocker;
import com.ticketstorm.lockinfo.LockInfoHandle;
import com.ticketstorm.lockinfo.factory.LockInfoHandleFactory;
import com.ticketstorm.lockinfo.impl.ServiceLockInfoHandle;
import com.ticketstorm.servicelock.aspect.ServiceLockAspect;
import com.ticketstorm.servicelock.factory.ServiceLockFactory;
import com.ticketstorm.util.ServiceLockTool;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;


public class ServiceLockAutoConfiguration {
    
    @Bean(LockInfoType.SERVICE_LOCK)
    public LockInfoHandle serviceLockInfoHandle(){
        return new ServiceLockInfoHandle();
    }
    
    @Bean
    public ManageLocker manageLocker(RedissonClient redissonClient){
        return new ManageLocker(redissonClient);
    }
    
    @Bean
    public ServiceLockFactory serviceLockFactory(ManageLocker manageLocker){
        return new ServiceLockFactory(manageLocker);
    }
    
    @Bean
    public ServiceLockAspect serviceLockAspect(LockInfoHandleFactory lockInfoHandleFactory,ServiceLockFactory serviceLockFactory){
        return new ServiceLockAspect(lockInfoHandleFactory,serviceLockFactory);
    }
    
    @Bean
    public ServiceLockTool serviceLockUtil(LockInfoHandleFactory lockInfoHandleFactory,ServiceLockFactory serviceLockFactory){
        return new ServiceLockTool(lockInfoHandleFactory,serviceLockFactory);
    }
}
