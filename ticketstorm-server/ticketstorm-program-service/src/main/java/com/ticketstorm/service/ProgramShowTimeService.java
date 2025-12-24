package com.ticketstorm.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketstorm.core.RedisKeyManage;
import com.ticketstorm.dto.ProgramShowTimeAddDto;
import com.ticketstorm.entity.Program;
import com.ticketstorm.entity.ProgramGroup;
import com.ticketstorm.entity.ProgramShowTime;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.mapper.ProgramGroupMapper;
import com.ticketstorm.mapper.ProgramMapper;
import com.ticketstorm.mapper.ProgramShowTimeMapper;
import com.ticketstorm.redis.RedisCache;
import com.ticketstorm.redis.RedisKeyBuild;
import com.ticketstorm.service.cache.local.LocalCacheProgramShowTime;
import com.ticketstorm.servicelock.LockType;
import com.ticketstorm.servicelock.annotion.ServiceLock;
import com.ticketstorm.util.DateUtils;
import com.ticketstorm.util.ServiceLockTool;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.ticketstorm.core.DistributedLockConstants.GET_PROGRAM_SHOW_TIME_LOCK;
import static com.ticketstorm.core.DistributedLockConstants.PROGRAM_SHOW_TIME_LOCK;


@Service
public class ProgramShowTimeService extends ServiceImpl<ProgramShowTimeMapper, ProgramShowTime> {
    
    @Autowired
    private UidGenerator uidGenerator;
    
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ProgramMapper programMapper;
    
    @Autowired
    private ProgramShowTimeMapper programShowTimeMapper;
    
    @Autowired
    private ProgramGroupMapper programGroupMapper;
    
    @Autowired
    private ServiceLockTool serviceLockTool;
    
    @Autowired
    private LocalCacheProgramShowTime localCacheProgramShowTime;
    
    
    @Transactional(rollbackFor = Exception.class)
    public Long add(ProgramShowTimeAddDto programShowTimeAddDto) {
        ProgramShowTime programShowTime = new ProgramShowTime();
        BeanUtil.copyProperties(programShowTimeAddDto,programShowTime);
        programShowTime.setId(uidGenerator.getUid());
        programShowTimeMapper.insert(programShowTime);
        return programShowTime.getId();
    }
    
    public ProgramShowTime selectProgramShowTimeByProgramIdMultipleCache(Long programId){
        return localCacheProgramShowTime.getCache(RedisKeyBuild.createRedisKey
                (RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey(),
                key -> selectProgramShowTimeByProgramId(programId));
    }
    
    public ProgramShowTime simpleSelectProgramShowTimeByProgramIdMultipleCache(Long programId){
        ProgramShowTime programShowTimeCache = localCacheProgramShowTime.getCache(RedisKeyBuild.createRedisKey(
                RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey());
        if (Objects.nonNull(programShowTimeCache)) {
            return programShowTimeCache;
        }
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME,
                programId), ProgramShowTime.class);
    }
    
    @ServiceLock(lockType= LockType.Read,name = PROGRAM_SHOW_TIME_LOCK,keys = {"#programId"})
    public ProgramShowTime selectProgramShowTimeByProgramId(Long programId){
        ProgramShowTime programShowTime = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME, 
                programId), ProgramShowTime.class);
        if (Objects.nonNull(programShowTime)) {
            return programShowTime;
        }
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_SHOW_TIME_LOCK, 
                new String[]{String.valueOf(programId)});
        lock.lock();
        try {
            programShowTime = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME,
                    programId), ProgramShowTime.class);
            if (Objects.isNull(programShowTime)) {
                LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper =
                        Wrappers.lambdaQuery(ProgramShowTime.class).eq(ProgramShowTime::getProgramId, programId);
                programShowTime = Optional.ofNullable(programShowTimeMapper.selectOne(programShowTimeLambdaQueryWrapper))
                        .orElseThrow(() -> new TicketStormFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST));
                redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME, programId),programShowTime
                        ,DateUtils.countBetweenSecond(DateUtils.now(),programShowTime.getShowTime()),TimeUnit.SECONDS);
            }
            return programShowTime;
        }finally {
            lock.unlock();   
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Set<Long> renewal(){
        Set<Long> programIdSet = new HashSet<>();
        LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper =
                Wrappers.lambdaQuery(ProgramShowTime.class).
                        le(ProgramShowTime::getShowTime, DateUtils.addDay(DateUtils.now(), 2));
        List<ProgramShowTime> programShowTimes = programShowTimeMapper.selectList(programShowTimeLambdaQueryWrapper);
        
        List<ProgramShowTime> newProgramShowTimes = new ArrayList<>(programShowTimes.size());
        
        for (ProgramShowTime programShowTime : programShowTimes) {
            programIdSet.add(programShowTime.getProgramId());
            Date oldShowTime = programShowTime.getShowTime();
            Date newShowTime = DateUtils.addMonth(oldShowTime, 1);
            Date nowDateTime = DateUtils.now();
            while (newShowTime.before(nowDateTime)) {
                newShowTime = DateUtils.addMonth(newShowTime, 1);
            }
            Date newShowDayTime = DateUtils.parseDateTime(DateUtils.formatDate(newShowTime) + " 00:00:00");
            ProgramShowTime updateProgramShowTime = new ProgramShowTime();
            updateProgramShowTime.setShowTime(newShowTime);
            updateProgramShowTime.setShowDayTime(newShowDayTime);
            updateProgramShowTime.setShowWeekTime(DateUtils.getWeekStr(newShowTime));
            LambdaUpdateWrapper<ProgramShowTime> programShowTimeLambdaUpdateWrapper =
                    Wrappers.lambdaUpdate(ProgramShowTime.class)
                            .eq(ProgramShowTime::getProgramId, programShowTime.getProgramId())
                            .eq(ProgramShowTime::getId,programShowTime.getId());
                    
            programShowTimeMapper.update(updateProgramShowTime,programShowTimeLambdaUpdateWrapper);
            
            ProgramShowTime newProgramShowTime = new ProgramShowTime();
            newProgramShowTime.setProgramId(programShowTime.getProgramId());
            newProgramShowTime.setShowTime(newShowTime);
            newProgramShowTimes.add(newProgramShowTime);
        }
        Map<Long,Date> programGroupMap = new HashMap<>(newProgramShowTimes.size());
        for (ProgramShowTime newProgramShowTime : newProgramShowTimes) {
            Program program = programMapper.selectById(newProgramShowTime.getProgramId());
            if (Objects.isNull(program)) {
                continue;
            }
            Long programGroupId = program.getProgramGroupId();
            Date showTime = programGroupMap.get(programGroupId);
            if (Objects.isNull(showTime)) {
                programGroupMap.put(programGroupId,newProgramShowTime.getShowTime());
            }else {
                if (DateUtil.compare(newProgramShowTime.getShowTime(),showTime) < 0) {
                    programGroupMap.put(programGroupId,newProgramShowTime.getShowTime());
                }
            }
        }
        if (CollectionUtil.isNotEmpty(programGroupMap)) {
            programGroupMap.forEach((k,v) -> {
                ProgramGroup programGroup = new ProgramGroup();
                programGroup.setRecentShowTime(v);
                
                LambdaUpdateWrapper<ProgramGroup> programGroupLambdaUpdateWrapper =
                        Wrappers.lambdaUpdate(ProgramGroup.class)
                                .eq(ProgramGroup::getId,k);
                programGroupMapper.update(programGroup,programGroupLambdaUpdateWrapper);
            });
        }
        
        return programIdSet;
    }
}
