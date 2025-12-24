package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.AreaGetDto;
import com.ticketstorm.dto.AreaSelectDto;
import com.ticketstorm.dto.GetChannelDataByCodeDto;
import com.ticketstorm.vo.AreaVo;
import com.ticketstorm.vo.GetChannelDataVo;
import com.ticketstorm.vo.TokenDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import static com.ticketstorm.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"base-data-service",fallback  = BaseDataClientFallback.class)
public interface BaseDataClient {
    /**
     * 根据code查询数据
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping("/channel/data/getByCode")
    ApiResponse<GetChannelDataVo> getByCode(GetChannelDataByCodeDto dto);
    
    /**
     * 查询token数据
     * @return 结果
     * */
    @PostMapping(value = "/get")
    ApiResponse<TokenDataVo> get();
    
    /**
     * 根据id集合查询地区列表
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/area/selectByIdList")
    ApiResponse<List<AreaVo>> selectByIdList(AreaSelectDto dto);
    
    /**
     * 根据id查询地区
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/area/getById")
    ApiResponse<AreaVo> getById(AreaGetDto dto);
}
