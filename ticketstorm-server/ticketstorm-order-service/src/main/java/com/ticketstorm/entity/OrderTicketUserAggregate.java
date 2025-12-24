package com.ticketstorm.entity;

import lombok.Data;


@Data
public class OrderTicketUserAggregate {
    
    private Long orderNumber;
    
    private Integer orderTicketUserCount;
}
