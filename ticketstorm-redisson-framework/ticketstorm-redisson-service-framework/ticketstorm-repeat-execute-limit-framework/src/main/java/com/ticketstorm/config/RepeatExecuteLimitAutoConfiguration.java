package com.ticketstorm.config;

import com.ticketstorm.constant.LockInfoType;
import com.ticketstorm.handle.RedissonDataHandle;
import com.ticketstorm.locallock.LocalLockCache;
import com.ticketstorm.lockinfo.LockInfoHandle;
import com.ticketstorm.lockinfo.factory.LockInfoHandleFactory;
import com.ticketstorm.lockinfo.impl.RepeatExecuteLimitLockInfoHandle;
import com.ticketstorm.repeatexecutelimit.aspect.RepeatExecuteLimitAspect;
import com.ticketstorm.servicelock.factory.ServiceLockFactory;
import org.springframework.context.annotation.Bean;


public class RepeatExecuteLimitAutoConfiguration {
    
    @Bean(LockInfoType.REPEAT_EXECUTE_LIMIT)
    public LockInfoHandle repeatExecuteLimitHandle(){
        return new RepeatExecuteLimitLockInfoHandle();
    }
    
    @Bean
    public RepeatExecuteLimitAspect repeatExecuteLimitAspect(LocalLockCache localLockCache,
                                                             LockInfoHandleFactory lockInfoHandleFactory,
                                                             ServiceLockFactory serviceLockFactory,
                                                             RedissonDataHandle redissonDataHandle){
        return new RepeatExecuteLimitAspect(localLockCache, lockInfoHandleFactory,serviceLockFactory,redissonDataHandle);
    }
}
    