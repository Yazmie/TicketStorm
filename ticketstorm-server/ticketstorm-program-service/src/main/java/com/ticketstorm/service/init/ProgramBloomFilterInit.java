package com.ticketstorm.service.init;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketstorm.handler.BloomFilterHandler;
import com.ticketstorm.initialize.base.AbstractApplicationPostConstructHandler;
import com.ticketstorm.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ProgramBloomFilterInit extends AbstractApplicationPostConstructHandler {
    
    @Autowired
    private ProgramService programService;
    
    @Autowired
    private BloomFilterHandler bloomFilterHandler;
    
    @Override
    public Integer executeOrder() {
        return 4;
    }
    
    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        List<Long> allProgramIdList = programService.getAllProgramIdList();
        if (CollectionUtil.isEmpty(allProgramIdList)) {
            return;
        }
        allProgramIdList.forEach(programId -> bloomFilterHandler.add(String.valueOf(programId)));
    }
}
