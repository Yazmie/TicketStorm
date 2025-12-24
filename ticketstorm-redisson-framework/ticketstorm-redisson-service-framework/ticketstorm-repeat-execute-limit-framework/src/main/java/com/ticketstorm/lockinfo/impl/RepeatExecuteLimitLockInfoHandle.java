package com.ticketstorm.lockinfo.impl;

import com.ticketstorm.lockinfo.AbstractLockInfoHandle;


public class RepeatExecuteLimitLockInfoHandle extends AbstractLockInfoHandle {

    public static final String PREFIX_NAME = "REPEAT_EXECUTE_LIMIT";
    
    @Override
    protected String getLockPrefixName() {
        return PREFIX_NAME;
    }
}
