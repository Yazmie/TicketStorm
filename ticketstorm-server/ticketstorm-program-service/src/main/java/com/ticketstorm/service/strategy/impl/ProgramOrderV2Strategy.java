package com.ticketstorm.service.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.ticketstorm.core.RepeatExecuteLimitConstants;
import com.ticketstorm.dto.ProgramOrderCreateDto;
import com.ticketstorm.dto.SeatDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.enums.ProgramOrderVersion;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.initialize.base.AbstractApplicationCommandLineRunnerHandler;
import com.ticketstorm.initialize.impl.composite.CompositeContainer;
import com.ticketstorm.locallock.LocalLockCache;
import com.ticketstorm.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketstorm.service.ProgramOrderService;
import com.ticketstorm.service.strategy.ProgramOrderContext;
import com.ticketstorm.service.strategy.ProgramOrderStrategy;
import com.ticketstorm.servicelock.LockType;
import com.ticketstorm.util.ServiceLockTool;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.ticketstorm.core.DistributedLockConstants.PROGRAM_ORDER_CREATE_V2;


@Slf4j
@Component
public class ProgramOrderV2Strategy extends AbstractApplicationCommandLineRunnerHandler implements ProgramOrderStrategy {
    
    @Autowired
    private ProgramOrderService programOrderService;
    
    @Autowired
    private ServiceLockTool serviceLockTool;
    
    @Autowired
    private CompositeContainer compositeContainer;
    
    @Autowired
    private LocalLockCache localLockCache;
    
    
    @RepeatExecuteLimit(
            name = RepeatExecuteLimitConstants.CREATE_PROGRAM_ORDER,
            keys = {"#programOrderCreateDto.userId","#programOrderCreateDto.programId"})
    @Override
    public String createOrder(ProgramOrderCreateDto programOrderCreateDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue(),programOrderCreateDto);
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<Long> ticketCategoryIdList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            ticketCategoryIdList =
                    seatDtoList.stream().map(SeatDto::getTicketCategoryId).distinct().sorted().collect(Collectors.toList());
        }else {
            ticketCategoryIdList.add(programOrderCreateDto.getTicketCategoryId());
        }
        List<ReentrantLock> localLockList = new ArrayList<>(ticketCategoryIdList.size());
        List<RLock> serviceLockList = new ArrayList<>(ticketCategoryIdList.size());
        List<ReentrantLock> localLockSuccessList = new ArrayList<>(ticketCategoryIdList.size());
        List<RLock> serviceLockSuccessList = new ArrayList<>(ticketCategoryIdList.size());
        for (Long ticketCategoryId : ticketCategoryIdList) {
            String lockKey = StrUtil.join("-",PROGRAM_ORDER_CREATE_V2,
                    programOrderCreateDto.getProgramId(),ticketCategoryId);
            ReentrantLock localLock = localLockCache.getLock(lockKey,false);
            RLock serviceLock = serviceLockTool.getLock(LockType.Reentrant, lockKey);
            localLockList.add(localLock);
            serviceLockList.add(serviceLock);
        }
        for (ReentrantLock reentrantLock : localLockList) {
            try {
                reentrantLock.lock();
            }catch (Throwable t) {
                break;
            }
            localLockSuccessList.add(reentrantLock);
        }
        boolean serviceLockFail = false;
        for (RLock rLock : serviceLockList) {
            try {
                rLock.lock();
            }catch (Throwable t) {
                serviceLockFail = true;
                break;
            }
            serviceLockSuccessList.add(rLock);
        }
        try {
            if (serviceLockFail) {
                throw new TicketStormFrameException(BaseCode.SERVICE_LOCK_FAIL);
            }
            return programOrderService.create(programOrderCreateDto);
        }finally {
            for (int i = serviceLockSuccessList.size() - 1; i >= 0; i--) {
                RLock rLock = serviceLockSuccessList.get(i);
                try {
                    rLock.unlock();
                }catch (Throwable t) {
                    log.error("service lock unlock error",t);
                }
            }
            for (int i = localLockSuccessList.size() - 1; i >= 0; i--) {
                ReentrantLock reentrantLock = localLockSuccessList.get(i);
                try {
                    reentrantLock.unlock();
                }catch (Throwable t) {
                    log.error("local lock unlock error",t);
                }
            }
        }
    }
    
    @Override
    public Integer executeOrder() {
        return 2;
    }
    
    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        ProgramOrderContext.add(ProgramOrderVersion.V2_VERSION.getVersion(),this);
    }
}
