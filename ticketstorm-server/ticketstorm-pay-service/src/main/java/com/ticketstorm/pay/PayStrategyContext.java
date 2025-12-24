package com.ticketstorm.pay;

import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class PayStrategyContext {
    
    private final Map<String,PayStrategyHandler> payStrategyHandlerMap = new HashMap<>();
    
    public void put(String channel,PayStrategyHandler payStrategyHandler){
        payStrategyHandlerMap.put(channel,payStrategyHandler);
    }
    
    public PayStrategyHandler get(String channel){
        return Optional.ofNullable(payStrategyHandlerMap.get(channel)).orElseThrow(
                () -> new TicketStormFrameException(BaseCode.PAY_STRATEGY_NOT_EXIST));
    }
}
