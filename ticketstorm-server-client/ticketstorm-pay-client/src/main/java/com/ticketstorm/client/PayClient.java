package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.NotifyDto;
import com.ticketstorm.dto.PayDto;
import com.ticketstorm.dto.RefundDto;
import com.ticketstorm.dto.TradeCheckDto;
import com.ticketstorm.vo.NotifyVo;
import com.ticketstorm.vo.TradeCheckVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import static com.ticketstorm.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"pay-service",fallback = PayClientFallback.class)
public interface PayClient {
    /**
     * 支付
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/pay/common/pay")
    ApiResponse<String> commonPay(PayDto dto);
    /**
     * 回调
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/pay/notify")
    ApiResponse<NotifyVo> notify(NotifyDto dto);
    /**
     * 查询支付状态
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/pay/trade/check")
    ApiResponse<TradeCheckVo> tradeCheck(TradeCheckDto dto);
    /**
     * 退款
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/pay/refund")
    ApiResponse<String> refund(RefundDto dto);
}
