package com.ticketstorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketstorm.dto.TicketCategoryCountDto;
import com.ticketstorm.entity.TicketCategory;
import com.ticketstorm.entity.TicketCategoryAggregate;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface TicketCategoryMapper extends BaseMapper<TicketCategory> {
    
    /**
     * 票档统计
     * @param programIdList 参数
     * @return 结果
     * */
    List<TicketCategoryAggregate> selectAggregateList(@Param("programIdList")List<Long> programIdList);
    
    /**
     * 更新数量
     * @param number 数量
     * @param id id
     * @return 结果
     * */
    int updateRemainNumber(@Param("number")Long number,@Param("id")Long id);
    
    /**
     * 批量更新数量
     * @param ticketCategoryCountDtoList 参数
     * @param programId 参数
     * @return 结果
     * */
    int batchUpdateRemainNumber(@Param("ticketCategoryCountDtoList") 
                                List<TicketCategoryCountDto> ticketCategoryCountDtoList,
                                @Param("programId")
                                Long programId);
}
