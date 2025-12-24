package com.ticketstorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketstorm.entity.Rule;


public interface RuleMapper extends BaseMapper<Rule> {
    
    /**
     * 删除所有规则
     * @return 结果
     * */
    int delAll();
}
