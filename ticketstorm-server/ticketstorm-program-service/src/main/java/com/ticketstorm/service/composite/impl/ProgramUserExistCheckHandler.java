package com.ticketstorm.service.composite.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.ticketstorm.client.OrderClient;
import com.ticketstorm.client.UserClient;
import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.core.RedisKeyManage;
import com.ticketstorm.dto.AccountOrderCountDto;
import com.ticketstorm.dto.ProgramGetDto;
import com.ticketstorm.dto.ProgramOrderCreateDto;
import com.ticketstorm.dto.TicketUserListDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.redis.RedisCache;
import com.ticketstorm.redis.RedisKeyBuild;
import com.ticketstorm.service.ProgramService;
import com.ticketstorm.service.composite.AbstractProgramCheckHandler;
import com.ticketstorm.service.tool.TokenExpireManager;
import com.ticketstorm.vo.AccountOrderCountVo;
import com.ticketstorm.vo.ProgramVo;
import com.ticketstorm.vo.TicketUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
public class ProgramUserExistCheckHandler extends AbstractProgramCheckHandler {
    
    @Autowired
    private UserClient userClient;
    
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private OrderClient orderClient;
    
    @Autowired
    private ProgramService programService;
    
    @Autowired
    private TokenExpireManager tokenExpireManager;
    
    @Override
    protected void execute(ProgramOrderCreateDto programOrderCreateDto) {
        List<TicketUserVo> ticketUserVoList = redisCache.getValueIsList(RedisKeyBuild.createRedisKey(
                RedisKeyManage.TICKET_USER_LIST, programOrderCreateDto.getUserId()), TicketUserVo.class);
        if (CollectionUtil.isEmpty(ticketUserVoList)) {
            TicketUserListDto ticketUserListDto = new TicketUserListDto();
            ticketUserListDto.setUserId(programOrderCreateDto.getUserId());
            ApiResponse<List<TicketUserVo>> apiResponse = userClient.list(ticketUserListDto);
            if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                ticketUserVoList = apiResponse.getData();
            }else {
                log.error("user client rpc getUserAndTicketUserList select response : {}", JSON.toJSONString(apiResponse));
                throw new TicketStormFrameException(apiResponse);
            }
        }
        if (CollectionUtil.isEmpty(ticketUserVoList)) {
            throw new TicketStormFrameException(BaseCode.TICKET_USER_EMPTY);
        }
        Map<Long, TicketUserVo> ticketUserVoMap = ticketUserVoList.stream()
                .collect(Collectors.toMap(TicketUserVo::getId, ticketUserVo -> ticketUserVo, (v1, v2) -> v2));
        for (Long ticketUserId : programOrderCreateDto.getTicketUserIdList()) {
            if (Objects.isNull(ticketUserVoMap.get(ticketUserId))) {
                throw new TicketStormFrameException(BaseCode.TICKET_USER_EMPTY);
            }
        }
        ProgramGetDto programGetDto = new ProgramGetDto();
        programGetDto.setId(programOrderCreateDto.getProgramId());
        ProgramVo programVo = programService.detailV2(programGetDto);
        if (Objects.isNull(programVo)) {
            throw new TicketStormFrameException(BaseCode.PROGRAM_NOT_EXIST);
        }
        Integer count = 0;
        if (redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,
                programOrderCreateDto.getUserId(),programOrderCreateDto.getProgramId()))) {
            count = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,
                    programOrderCreateDto.getUserId(),programOrderCreateDto.getProgramId()), Integer.class);
        }else {
            AccountOrderCountDto accountOrderCountDto = new AccountOrderCountDto();
            accountOrderCountDto.setUserId(programOrderCreateDto.getUserId());
            accountOrderCountDto.setProgramId(programOrderCreateDto.getProgramId());
            ApiResponse<AccountOrderCountVo> apiResponse = orderClient.accountOrderCount(accountOrderCountDto);
            if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                count = Optional.ofNullable(apiResponse.getData()).map(AccountOrderCountVo::getCount).orElse(0);
                redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,
                                programOrderCreateDto.getUserId(),
                                programOrderCreateDto.getProgramId()),
                        count, tokenExpireManager.getTokenExpireTime() + 1, TimeUnit.MINUTES);
            }
        }
        
        Integer seatCount = Optional.ofNullable(programOrderCreateDto.getSeatDtoList()).map(List::size).orElse(0);
        
        Integer ticketCount = Optional.ofNullable(programOrderCreateDto.getTicketCount()).orElse(0);
        if (seatCount != 0) {
            count = count + seatCount;
        }else if (ticketCount != 0) {
            count = count + ticketCount;
        }
//        if (count > programVo.getPerAccountLimitPurchaseCount()) {
//            throw new TicketStormFrameException(BaseCode.PER_ACCOUNT_PURCHASE_COUNT_OVER_LIMIT);
//        }
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 2;
    }
    
    @Override
    public Integer executeOrder() {
        return 2;
    }
}
