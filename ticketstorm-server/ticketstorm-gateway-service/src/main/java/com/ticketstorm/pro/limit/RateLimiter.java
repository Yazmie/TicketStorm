package com.ticketstorm.pro.limit;

import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class RateLimiter {
    
    private final Semaphore semaphore;
    private final TimeUnit timeUnit;
    
    public RateLimiter(int maxPermitsPerSecond) {
        this.timeUnit = TimeUnit.SECONDS;
        this.semaphore = new Semaphore(maxPermitsPerSecond);
    }
    
    public void acquire() throws InterruptedException {
        if (!semaphore.tryAcquire(1, timeUnit)) {
            throw new TicketStormFrameException(BaseCode.OPERATION_IS_TOO_FREQUENT_PLEASE_TRY_AGAIN_LATER);
        }
    }
    
    public void release() {
        semaphore.release();
    }
}
