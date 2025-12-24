package com.ticketstorm.service.strategy.impl;

import com.ticketstorm.core.RepeatExecuteLimitConstants;
import com.ticketstorm.dto.ProgramOrderCreateDto;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.enums.ProgramOrderVersion;
import com.ticketstorm.initialize.base.AbstractApplicationCommandLineRunnerHandler;
import com.ticketstorm.initialize.impl.composite.CompositeContainer;
import com.ticketstorm.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketstorm.service.ProgramOrderService;
import com.ticketstorm.service.strategy.ProgramOrderContext;
import com.ticketstorm.service.strategy.ProgramOrderStrategy;
import com.ticketstorm.servicelock.annotion.ServiceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static com.ticketstorm.core.DistributedLockConstants.PROGRAM_ORDER_CREATE_V1;


@Component
public class ProgramOrderV1Strategy extends AbstractApplicationCommandLineRunnerHandler implements ProgramOrderStrategy {
    
    @Autowired
    private ProgramOrderService programOrderService;
    
    @Autowired
    private CompositeContainer compositeContainer;
    
    
    @RepeatExecuteLimit(
            name = RepeatExecuteLimitConstants.CREATE_PROGRAM_ORDER,
            keys = {"#programOrderCreateDto.userId","#programOrderCreateDto.programId"})
    @ServiceLock(name = PROGRAM_ORDER_CREATE_V1,keys = {"#programOrderCreateDto.programId"})
    @Override
    public String createOrder(final ProgramOrderCreateDto programOrderCreateDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue(),programOrderCreateDto);
        return programOrderService.create(programOrderCreateDto);
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
    
    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        ProgramOrderContext.add(ProgramOrderVersion.V1_VERSION.getVersion(),this);
    }
}
