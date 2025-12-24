package com.ticketstorm.service.composite;

import com.ticketstorm.dto.ProgramOrderCreateDto;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.initialize.impl.composite.AbstractComposite;


public abstract class AbstractProgramCheckHandler extends AbstractComposite<ProgramOrderCreateDto> {
    
    @Override
    public String type() {
        return CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue();
    }
}
