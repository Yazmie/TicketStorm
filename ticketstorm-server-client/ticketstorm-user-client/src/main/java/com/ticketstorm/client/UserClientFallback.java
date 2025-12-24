package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.TicketUserListDto;
import com.ticketstorm.dto.UserGetAndTicketUserListDto;
import com.ticketstorm.dto.UserIdDto;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.vo.UserGetAndTicketUserListVo;
import com.ticketstorm.vo.TicketUserVo;
import com.ticketstorm.vo.UserVo;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public ApiResponse<UserVo> getById(final UserIdDto userIdDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<List<TicketUserVo>> list(final TicketUserListDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<UserGetAndTicketUserListVo> getUserAndTicketUserList(final UserGetAndTicketUserListDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
