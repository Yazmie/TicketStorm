package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.TicketCategoryListByProgramDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.vo.TicketCategoryDetailVo;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ProgramClientFallback implements ProgramClient {
    
    @Override
    public ApiResponse<List<TicketCategoryDetailVo>> selectListByProgram(TicketCategoryListByProgramDto ticketCategoryListByProgramDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
