package com.ticketstorm.shardingsphere;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class TableOrderComplexGeneArithmetic implements ComplexKeysShardingAlgorithm<Long> {
    
   
    private static final String SHARDING_COUNT_KEY_NAME = "sharding-count";
    
    private int shardingCount;
    
    @Override
    public void init(Properties props) {
        shardingCount = Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY_NAME));
    }
    @Override
    public Collection<String> doSharding(Collection<String> allActualSplitTableNames, ComplexKeysShardingValue<Long> complexKeysShardingValue) {
        List<String> actualTableNames = new ArrayList<>(allActualSplitTableNames.size());
        String logicTableName = complexKeysShardingValue.getLogicTableName();
        Map<String, Collection<Long>> columnNameAndShardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        if (CollectionUtil.isEmpty(columnNameAndShardingValuesMap)) {
            return allActualSplitTableNames;
        }
        Collection<Long> orderNumberValues = columnNameAndShardingValuesMap.get("order_number");
        Collection<Long> userIdValues = columnNameAndShardingValuesMap.get("user_id");
        
        Long value = null;
        if (CollectionUtil.isNotEmpty(orderNumberValues)) {
            value = orderNumberValues.stream().findFirst().orElseThrow(() -> new TicketStormFrameException(BaseCode.ORDER_NUMBER_NOT_EXIST));
        } else if (CollectionUtil.isNotEmpty(userIdValues)) {
            value = userIdValues.stream().findFirst().orElseThrow(() -> new TicketStormFrameException(BaseCode.USER_ID_NOT_EXIST));
        }
        if (Objects.nonNull(value)) {
            actualTableNames.add(logicTableName + "_" + ((shardingCount - 1) & value));
            return actualTableNames;
        }
        return allActualSplitTableNames;
    }
}
