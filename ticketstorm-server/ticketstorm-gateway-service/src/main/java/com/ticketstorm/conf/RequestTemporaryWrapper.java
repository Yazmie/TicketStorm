package com.ticketstorm.conf;

import com.ticketstorm.common.ApiResponse;
import lombok.Data;

import java.util.Map;


@Data
public class RequestTemporaryWrapper {
    
    private Map<String,String> map;
    
    private ApiResponse<?> apiResponse;
}
