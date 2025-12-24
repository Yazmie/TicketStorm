package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.AccountOrderCountDto;
import com.ticketstorm.dto.OrderCreateDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.vo.AccountOrderCountVo;
import org.springframework.stereotype.Component;


@Component
public class OrderClientFallback implements OrderClient {
    
    @Override
    public ApiResponse<String> create(final OrderCreateDto orderCreateDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<AccountOrderCountVo> accountOrderCount(final AccountOrderCountDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
