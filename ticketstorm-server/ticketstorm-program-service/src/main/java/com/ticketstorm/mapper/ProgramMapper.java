package com.ticketstorm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticketstorm.dto.ProgramListDto;
import com.ticketstorm.dto.ProgramPageListDto;
import com.ticketstorm.entity.Program;
import com.ticketstorm.entity.ProgramJoinShowTime;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ProgramMapper extends BaseMapper<Program> {
    
    /**
     * 主页查询
     * @param programListDto 参数
     * @return 结果
     * */
    List<Program> selectHomeList(@Param("programListDto")ProgramListDto programListDto);
    
    /**
     * 分页查询
     * @param page 分页对象
     * @param programPageListDto 参数
     * @return 结果
     * */
    IPage<ProgramJoinShowTime> selectPage(IPage<ProgramJoinShowTime> page, 
                                          @Param("programPageListDto")ProgramPageListDto programPageListDto);
}
