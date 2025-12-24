package com.ticketstorm.client;

import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.dto.TicketUserListDto;
import com.ticketstorm.dto.UserGetAndTicketUserListDto;
import com.ticketstorm.dto.UserIdDto;
import com.ticketstorm.vo.TicketUserVo;
import com.ticketstorm.vo.UserGetAndTicketUserListVo;
import com.ticketstorm.vo.UserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import static com.ticketstorm.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"user-service",fallback = UserClientFallback.class)
public interface UserClient {
    
    /**
     * 查询用户(通过id)
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/user/getById")
    ApiResponse<UserVo> getById(UserIdDto dto);
    

    /**
     * 查询购票人(通过userId)
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/ticket/user/list")
    ApiResponse<List<TicketUserVo>> list(TicketUserListDto dto);
    
    /**
     * 查询用户和购票人集合
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/user/get/user/ticket/list")
    ApiResponse<UserGetAndTicketUserListVo> getUserAndTicketUserList(UserGetAndTicketUserListDto dto);
    
}
