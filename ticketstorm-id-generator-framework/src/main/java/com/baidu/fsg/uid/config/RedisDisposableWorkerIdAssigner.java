package com.baidu.fsg.uid.config;

import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;


public class RedisDisposableWorkerIdAssigner implements WorkerIdAssigner {
    
    private RedisTemplate redisTemplate;
    
    public RedisDisposableWorkerIdAssigner (RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public long assignWorkerId() {
        String key = "uid_work_id";
        Long increment = redisTemplate.opsForValue().increment(key);
        return Optional.ofNullable(increment).orElseThrow(() -> new TicketStormFrameException(BaseCode.UID_WORK_ID_ERROR));
    }
}
