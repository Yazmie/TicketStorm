package com.ticketstorm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketstorm.data.BaseTableData;
import lombok.Data;

import java.io.Serializable;


@Data
@TableName("d_rule")
public class Rule extends BaseTableData implements Serializable {
    
    private Long id;

    private Integer statTime;
    
    private Integer statTimeType;
    
    private Integer threshold;
    
    private Integer effectiveTime;
    
    private Integer effectiveTimeType;
    
    private String limitApi;
    
    private String message;
}
