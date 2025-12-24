package com.ticketstorm.service.composite.register.impl;

import com.ticketstorm.dto.UserRegisterDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.service.composite.register.AbstractUserRegisterCheckHandler;
import com.ticketstorm.service.tool.RequestCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class UserRegisterCountCheckHandler extends AbstractUserRegisterCheckHandler {
    
    @Autowired
    private RequestCounter requestCounter;
    
    @Override
    protected void execute(final UserRegisterDto param) {
        boolean result = requestCounter.onRequest();
        if (result) {
            throw new TicketStormFrameException(BaseCode.USER_REGISTER_FREQUENCY);
        }
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 2;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}
