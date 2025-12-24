package com.ticketstorm.service.composite.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketstorm.dto.ProgramOrderCreateDto;
import com.ticketstorm.dto.SeatDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.service.composite.AbstractProgramCheckHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class ProgramOrderCreateParamCheckHandler extends AbstractProgramCheckHandler {
    
    @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<Long> ticketUserIdList = programOrderCreateDto.getTicketUserIdList();
        Map<Long, List<Long>> ticketUserIdMap = 
                ticketUserIdList.stream().collect(Collectors.groupingBy(ticketUserId -> ticketUserId));
        for (List<Long> value : ticketUserIdMap.values()) {
            if (value.size() > 1) {
                throw new TicketStormFrameException(BaseCode.TICKET_USER_ID_REPEAT);
            }
        }
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            if (seatDtoList.size() != programOrderCreateDto.getTicketUserIdList().size()) {
                throw new TicketStormFrameException(BaseCode.TICKET_USER_COUNT_UNEQUAL_SEAT_COUNT);
            }
            for (SeatDto seatDto : seatDtoList) {
                if (Objects.isNull(seatDto.getId())) {
                    throw new TicketStormFrameException(BaseCode.SEAT_ID_EMPTY);
                }
                if (Objects.isNull(seatDto.getTicketCategoryId())) {
                    throw new TicketStormFrameException(BaseCode.SEAT_TICKET_CATEGORY_ID_EMPTY);
                }
                if (Objects.isNull(seatDto.getRowCode())) {
                    throw new TicketStormFrameException(BaseCode.SEAT_ROW_CODE_EMPTY);
                }
                if (Objects.isNull(seatDto.getColCode())) {
                    throw new TicketStormFrameException(BaseCode.SEAT_COL_CODE_EMPTY);
                }
                if (Objects.isNull(seatDto.getPrice())) {
                    throw new TicketStormFrameException(BaseCode.SEAT_PRICE_EMPTY);
                }
            }
        }else {
            if (Objects.isNull(programOrderCreateDto.getTicketCategoryId())) {
                throw new TicketStormFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST);
            }
            if (Objects.isNull(programOrderCreateDto.getTicketCount())) {
                throw new TicketStormFrameException(BaseCode.TICKET_COUNT_NOT_EXIST);
            }
            if (programOrderCreateDto.getTicketCount() <= 0) {
                throw new TicketStormFrameException(BaseCode.TICKET_COUNT_ERROR);
            }
        }
    }
    
    @Override
    public Integer executeParentOrder() {
        return 0;
    }
    
    @Override
    public Integer executeTier() {
        return 1;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}
