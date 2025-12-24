package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.AreaGetDto;
import com.ticketstorm.dto.AreaSelectDto;
import com.ticketstorm.dto.GetChannelDataByCodeDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.vo.AreaVo;
import com.ticketstorm.vo.GetChannelDataVo;
import com.ticketstorm.vo.TokenDataVo;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class BaseDataClientFallback implements BaseDataClient{
    @Override
    public ApiResponse<GetChannelDataVo> getByCode(final GetChannelDataByCodeDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<TokenDataVo> get() {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<List<AreaVo>> selectByIdList(final AreaSelectDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<AreaVo> getById(final AreaGetDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
