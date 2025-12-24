package com.ticketstorm.controller;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.SeatAddDto;
import com.ticketstorm.dto.SeatBatchAddDto;
import com.ticketstorm.dto.SeatListDto;
import com.ticketstorm.service.SeatService;
import com.ticketstorm.vo.SeatRelateInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/seat")
@Tag(name = "seat", description = "座位")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    
    @Operation(summary  = "单个座位添加")
    @PostMapping(value = "/add")
    public ApiResponse<Long> add(@Valid @RequestBody SeatAddDto seatAddDto) {
        return ApiResponse.ok(seatService.add(seatAddDto));
    }
    
    @Operation(summary  = "批量座位添加")
    @PostMapping(value = "/batch/add")
    public ApiResponse<Boolean> batchAdd(@Valid @RequestBody SeatBatchAddDto seatBatchAddDto) {
        return ApiResponse.ok(seatService.batchAdd(seatBatchAddDto));
    }
    
    @Operation(summary  = "查询座位相关信息")
    @PostMapping(value = "/relate/info")
    public ApiResponse<SeatRelateInfoVo> relateInfo(@Valid @RequestBody SeatListDto seatListDto) {
        return ApiResponse.ok(seatService.relateInfo(seatListDto));
    }
}
