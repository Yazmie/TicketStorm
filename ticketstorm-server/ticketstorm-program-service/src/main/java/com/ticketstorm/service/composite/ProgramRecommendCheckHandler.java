package com.ticketstorm.service.composite;

import com.ticketstorm.dto.ProgramRecommendListDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.initialize.impl.composite.AbstractComposite;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class ProgramRecommendCheckHandler extends AbstractComposite<ProgramRecommendListDto> {
    
    @Override
    protected void execute(final ProgramRecommendListDto param) {
        if (Objects.isNull(param.getAreaId()) && 
                Objects.isNull(param.getParentProgramCategoryId()) &&
                Objects.isNull(param.getProgramId())) {
            throw new TicketStormFrameException(BaseCode.PARAMETERS_CANNOT_BE_EMPTY);
        }
    }
    
    @Override
    public String type() {
        return CompositeCheckType.PROGRAM_RECOMMEND_CHECK.getValue();
    }
    
    @Override
    public Integer executeParentOrder() {
        return 0;
    }
    
    @Override
    public Integer executeTier() {
        return 1;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}
