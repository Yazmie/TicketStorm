package com.ticketstorm.service.strategy.impl;

import com.ticketstorm.core.RepeatExecuteLimitConstants;
import com.ticketstorm.dto.ProgramOrderCreateDto;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.enums.ProgramOrderVersion;
import com.ticketstorm.initialize.base.AbstractApplicationCommandLineRunnerHandler;
import com.ticketstorm.initialize.impl.composite.CompositeContainer;
import com.ticketstorm.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketstorm.service.ProgramOrderService;
import com.ticketstorm.service.strategy.BaseProgramOrder;
import com.ticketstorm.service.strategy.ProgramOrderContext;
import com.ticketstorm.service.strategy.ProgramOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static com.ticketstorm.core.DistributedLockConstants.PROGRAM_ORDER_CREATE_V3;


@Slf4j
@Component
public class ProgramOrderV3Strategy extends AbstractApplicationCommandLineRunnerHandler implements ProgramOrderStrategy {
    
    @Autowired
    private ProgramOrderService programOrderService;
    
    @Autowired
    private BaseProgramOrder baseProgramOrder;
    
    @Autowired
    private CompositeContainer compositeContainer;
    
    @RepeatExecuteLimit(
            name = RepeatExecuteLimitConstants.CREATE_PROGRAM_ORDER,
            keys = {"#programOrderCreateDto.userId","#programOrderCreateDto.programId"})
    @Override
    public String createOrder(ProgramOrderCreateDto programOrderCreateDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue(),programOrderCreateDto);
        return baseProgramOrder.localLockCreateOrder(PROGRAM_ORDER_CREATE_V3,programOrderCreateDto,
                () -> programOrderService.createNew(programOrderCreateDto));
    }
    
    @Override
    public Integer executeOrder() {
        return 3;
    }
    
    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        ProgramOrderContext.add(ProgramOrderVersion.V3_VERSION.getVersion(),this);
    }
}
