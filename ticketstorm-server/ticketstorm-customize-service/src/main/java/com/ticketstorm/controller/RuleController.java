package com.ticketstorm.controller;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.RuleDto;
import com.ticketstorm.dto.RuleGetDto;
import com.ticketstorm.dto.RuleStatusDto;
import com.ticketstorm.dto.RuleUpdateDto;
import com.ticketstorm.service.RuleService;
import com.ticketstorm.vo.RuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rule")
@Tag(name = "rule", description = "规则")
public class RuleController {

    @Autowired
    private RuleService ruleService;
    
    @Operation(summary  = "添加普通规则")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ApiResponse<Void> add(@Valid @RequestBody RuleDto ruleDto) {
        ruleService.ruleAdd(ruleDto);
        return ApiResponse.ok();
    }
    
    @Operation(summary  = "修改普通规则")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ApiResponse<Void> update(@Valid @RequestBody RuleUpdateDto ruleUpdateDto) {
        ruleService.ruleUpdate(ruleUpdateDto);
        return ApiResponse.ok();
    }
    
    @Operation(summary  = "修改普通规则状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    public ApiResponse<Void> updateStatus(@Valid @RequestBody RuleStatusDto ruleStatusDto){
        ruleService.ruleUpdateStatus(ruleStatusDto);
        return ApiResponse.ok();
    }
    
    @Operation(summary  = "查询普通规则")
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public ApiResponse<RuleVo> get(@Valid @RequestBody RuleGetDto ruleGetDto){
        return ApiResponse.ok(ruleService.get(ruleGetDto));
    }
}
