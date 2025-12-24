package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.NotifyDto;
import com.ticketstorm.dto.PayDto;
import com.ticketstorm.dto.RefundDto;
import com.ticketstorm.dto.TradeCheckDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.vo.NotifyVo;
import com.ticketstorm.vo.TradeCheckVo;
import org.springframework.stereotype.Component;


@Component
public class PayClientFallback implements PayClient{
    
    @Override
    public ApiResponse<String> commonPay(final PayDto payDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<NotifyVo> notify(final NotifyDto notifyDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<TradeCheckVo> tradeCheck(final TradeCheckDto tradeCheckDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<String> refund(final RefundDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
