package com.ticketstorm.service.lua;

import com.ticketstorm.vo.SeatVo;
import lombok.Data;

import java.util.List;


@Data
public class ProgramCacheCreateOrderData {

    private Integer code;
    
    private List<SeatVo> purchaseSeatList;
}
