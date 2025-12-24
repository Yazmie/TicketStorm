package com.ticketstorm.service.scheduletask;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketstorm.BusinessThreadPool;
import com.ticketstorm.dto.ProgramResetExecuteDto;
import com.ticketstorm.service.ProgramService;
import com.ticketstorm.service.init.ProgramElasticsearchInitData;
import com.ticketstorm.service.init.ProgramShowTimeRenewal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class ProgramDataTask {
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Autowired
    private ProgramService programService;
    
    @Autowired
    private ProgramShowTimeRenewal programShowTimeRenewal;
    
    @Autowired
    private ProgramElasticsearchInitData programElasticsearchInitData;
    
    
    @Scheduled(cron = "0 0 23 * * ?")
    public void executeTask(){
        BusinessThreadPool.execute( () -> {
            try {
                log.warn("节目服务定时任务重置执行");
                List<Long> allProgramIdList = programService.getAllProgramIdList();
                if (CollectionUtil.isNotEmpty(allProgramIdList)) {
                    for (Long programId : allProgramIdList) {
                        ProgramResetExecuteDto programResetExecuteDto = new ProgramResetExecuteDto();
                        programResetExecuteDto.setProgramId(programId);
                        programService.resetExecute(programResetExecuteDto);
                    }
                }
                programShowTimeRenewal.executeInit(applicationContext);
                programElasticsearchInitData.executeInit(applicationContext);
                
            }catch (Exception e) {
                log.error("executeTask error",e);
            }
        });
    }
}
