package com.ticketstorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketstorm.entity.DepthRule;


public interface DepthRuleMapper extends BaseMapper<DepthRule> {
    
    /**
     * 删除所有规则
     * @return 结果
     * */
    int delAll();
}
