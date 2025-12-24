package com.ticketstorm.service;

import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketstorm.core.RedisKeyManage;
import com.ticketstorm.dto.ChannelDataAddDto;
import com.ticketstorm.dto.GetChannelDataByCodeDto;
import com.ticketstorm.entity.ChannelTableData;
import com.ticketstorm.enums.Status;
import com.ticketstorm.mapper.ChannelDataMapper;
import com.ticketstorm.redis.RedisCache;
import com.ticketstorm.redis.RedisKeyBuild;
import com.ticketstorm.util.DateUtils;
import com.ticketstorm.vo.GetChannelDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Slf4j
public class ChannelDataService {
    
    @Autowired
    private ChannelDataMapper channelDataMapper;
    
    @Autowired
    private UidGenerator uidGenerator;
    
    @Autowired
    private RedisCache redisCache; 
    
    public GetChannelDataVo getByCode(GetChannelDataByCodeDto dto){
        GetChannelDataVo getChannelDataVo = new GetChannelDataVo();
        LambdaQueryWrapper<ChannelTableData> wrapper = Wrappers.lambdaQuery(ChannelTableData.class)
                .eq(ChannelTableData::getStatus, Status.RUN.getCode())
                .eq(ChannelTableData::getCode,dto.getCode());
        Optional.ofNullable(channelDataMapper.selectOne(wrapper)).ifPresent(channelData -> {
            BeanUtils.copyProperties(channelData,getChannelDataVo);
        });
        return getChannelDataVo;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void add(ChannelDataAddDto channelDataAddDto) {
        ChannelTableData channelData = new ChannelTableData();
        BeanUtils.copyProperties(channelDataAddDto,channelData);
        channelData.setId(uidGenerator.getUid());
        channelData.setCreateTime(DateUtils.now());
        channelDataMapper.insert(channelData);
        addRedisChannelData(channelData);
    }
    
    private void addRedisChannelData(ChannelTableData channelData){
        GetChannelDataVo getChannelDataVo = new GetChannelDataVo();
        BeanUtils.copyProperties(channelData,getChannelDataVo);
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.CHANNEL_DATA,getChannelDataVo.getCode()),getChannelDataVo);
    }
}
