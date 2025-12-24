package com.ticketstorm.service.strategy;

import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ProgramOrderContext {
    
    private static final Map<String,ProgramOrderStrategy> MAP = new HashMap<>(8);
    
    public static void add(String version,ProgramOrderStrategy programOrderStrategy){
        MAP.put(version,programOrderStrategy);
    }
    
    public static ProgramOrderStrategy get(String version){
        return Optional.ofNullable(MAP.get(version)).orElseThrow(() -> 
                new TicketStormFrameException(BaseCode.PROGRAM_ORDER_STRATEGY_NOT_EXIST));
    }
}
