package com.ticketstorm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketstorm.BusinessThreadPool;
import com.ticketstorm.RedisStreamPushHandler;
import com.ticketstorm.client.BaseDataClient;
import com.ticketstorm.client.OrderClient;
import com.ticketstorm.client.UserClient;
import com.ticketstorm.common.ApiResponse;
import com.ticketstorm.core.RedisKeyManage;
import com.ticketstorm.dto.AccountOrderCountDto;
import com.ticketstorm.dto.AreaGetDto;
import com.ticketstorm.dto.AreaSelectDto;
import com.ticketstorm.dto.ProgramAddDto;
import com.ticketstorm.dto.ProgramGetDto;
import com.ticketstorm.dto.ProgramInvalidDto;
import com.ticketstorm.dto.ProgramListDto;
import com.ticketstorm.dto.ProgramOperateDataDto;
import com.ticketstorm.dto.ProgramPageListDto;
import com.ticketstorm.dto.ProgramRecommendListDto;
import com.ticketstorm.dto.ProgramResetExecuteDto;
import com.ticketstorm.dto.ProgramSearchDto;
import com.ticketstorm.dto.TicketCategoryCountDto;
import com.ticketstorm.dto.TicketUserListDto;
import com.ticketstorm.entity.Program;
import com.ticketstorm.entity.ProgramCategory;
import com.ticketstorm.entity.ProgramGroup;
import com.ticketstorm.entity.ProgramJoinShowTime;
import com.ticketstorm.entity.ProgramShowTime;
import com.ticketstorm.entity.Seat;
import com.ticketstorm.entity.TicketCategory;
import com.ticketstorm.entity.TicketCategoryAggregate;
import com.ticketstorm.enums.BaseCode;
import com.ticketstorm.enums.BusinessStatus;
import com.ticketstorm.enums.CompositeCheckType;
import com.ticketstorm.enums.SellStatus;
import com.ticketstorm.exception.TicketStormFrameException;
import com.ticketstorm.initialize.impl.composite.CompositeContainer;
import com.ticketstorm.mapper.ProgramCategoryMapper;
import com.ticketstorm.mapper.ProgramGroupMapper;
import com.ticketstorm.mapper.ProgramMapper;
import com.ticketstorm.mapper.ProgramShowTimeMapper;
import com.ticketstorm.mapper.SeatMapper;
import com.ticketstorm.mapper.TicketCategoryMapper;
import com.ticketstorm.page.PageUtil;
import com.ticketstorm.page.PageVo;
import com.ticketstorm.redis.RedisCache;
import com.ticketstorm.redis.RedisKeyBuild;
import com.ticketstorm.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketstorm.service.cache.local.LocalCacheProgram;
import com.ticketstorm.service.cache.local.LocalCacheProgramCategory;
import com.ticketstorm.service.cache.local.LocalCacheProgramGroup;
import com.ticketstorm.service.cache.local.LocalCacheProgramShowTime;
import com.ticketstorm.service.cache.local.LocalCacheTicketCategory;
import com.ticketstorm.service.constant.ProgramTimeType;
import com.ticketstorm.service.es.ProgramEs;
import com.ticketstorm.service.lua.ProgramDelCacheData;
import com.ticketstorm.service.tool.TokenExpireManager;
import com.ticketstorm.servicelock.LockType;
import com.ticketstorm.servicelock.annotion.ServiceLock;
import com.ticketstorm.threadlocal.BaseParameterHolder;
import com.ticketstorm.util.DateUtils;
import com.ticketstorm.util.ServiceLockTool;
import com.ticketstorm.util.StringUtil;
import com.ticketstorm.vo.AccountOrderCountVo;
import com.ticketstorm.vo.AreaVo;
import com.ticketstorm.vo.ProgramGroupVo;
import com.ticketstorm.vo.ProgramHomeVo;
import com.ticketstorm.vo.ProgramListVo;
import com.ticketstorm.vo.ProgramSimpleInfoVo;
import com.ticketstorm.vo.ProgramVo;
import com.ticketstorm.vo.TicketCategoryVo;
import com.ticketstorm.vo.TicketUserVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ticketstorm.constant.Constant.CODE;
import static com.ticketstorm.constant.Constant.USER_ID;
import static com.ticketstorm.core.DistributedLockConstants.GET_PROGRAM_LOCK;
import static com.ticketstorm.core.DistributedLockConstants.PROGRAM_GROUP_LOCK;
import static com.ticketstorm.core.DistributedLockConstants.PROGRAM_LOCK;
import static com.ticketstorm.core.RepeatExecuteLimitConstants.CANCEL_PROGRAM_ORDER;
import static com.ticketstorm.util.DateUtils.FORMAT_DATE;


@Slf4j
@Service
public class ProgramService extends ServiceImpl<ProgramMapper, Program> {
    
    @Autowired
    private UidGenerator uidGenerator;
    
    @Autowired
    private ProgramMapper programMapper;
    
    @Autowired
    private ProgramGroupMapper programGroupMapper;
    
    @Autowired
    private ProgramShowTimeMapper programShowTimeMapper;
    
    @Autowired
    private ProgramCategoryMapper programCategoryMapper; 
    
    @Autowired
    private TicketCategoryMapper ticketCategoryMapper;
    
    @Autowired
    private SeatMapper seatMapper;
    
    @Autowired
    private BaseDataClient baseDataClient;
    
    @Autowired
    private UserClient userClient;
    
    @Autowired
    private OrderClient orderClient;
    
    @Autowired
    private RedisCache redisCache;
    
    @Lazy
    @Autowired
    private ProgramService programService;
    
    @Autowired
    private ProgramShowTimeService programShowTimeService;
    
    @Autowired
    private TicketCategoryService ticketCategoryService;
    
    @Autowired
    private ProgramCategoryService programCategoryService;
    
    @Autowired
    private ProgramEs programEs;
    
    @Autowired
    private ServiceLockTool serviceLockTool;
    
    @Autowired
    private RedisStreamPushHandler redisStreamPushHandler;
    
    @Autowired
    private LocalCacheProgram localCacheProgram;
    
    @Autowired
    private LocalCacheProgramGroup localCacheProgramGroup;
    
    @Autowired
    private LocalCacheProgramCategory localCacheProgramCategory;
    
    @Autowired
    private LocalCacheProgramShowTime localCacheProgramShowTime;
    
    @Autowired
    private LocalCacheTicketCategory localCacheTicketCategory;
    
    @Autowired
    private CompositeContainer compositeContainer;
    
    @Autowired
    private TokenExpireManager tokenExpireManager;
    
    @Autowired
    private ProgramDelCacheData programDelCacheData;
    
    /**
     * 添加节目
     * @param programAddDto 添加节目数据的入参
     * @return 添加节目后的id
     * */
    public Long add(ProgramAddDto programAddDto){
        Program program = new Program();
        BeanUtil.copyProperties(programAddDto,program);
        program.setId(uidGenerator.getUid());
        programMapper.insert(program);
        return program.getId();
    }
    
    /**
     * 搜索
     * @param programSearchDto 搜索节目数据的入参
     * @return 执行后的结果
     * */
    public PageVo<ProgramListVo> search(ProgramSearchDto programSearchDto) {
        //将入参的参数进行具体的组装
        setQueryTime(programSearchDto);
        return programEs.search(programSearchDto);
    }
    
    /**
     * 查询主页信息
     * @param programListDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public List<ProgramHomeVo> selectHomeList(ProgramListDto programListDto) {
        
        List<ProgramHomeVo> programHomeVoList = programEs.selectHomeList(programListDto);
        if (CollectionUtil.isNotEmpty(programHomeVoList)) {
            return programHomeVoList;
        }
        return dbSelectHomeList(programListDto);
    }
    
    /**
     * 查询主页信息（数据库查询）
     * @param programPageListDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    private List<ProgramHomeVo> dbSelectHomeList(ProgramListDto programPageListDto){
        List<ProgramHomeVo> programHomeVoList = new ArrayList<>();
        Map<Long, String> programCategoryMap = selectProgramCategoryMap(programPageListDto.getParentProgramCategoryIds());
        
        List<Program> programList = programMapper.selectHomeList(programPageListDto);
        if (CollectionUtil.isEmpty(programList)) {
            return programHomeVoList;
        }
        
        List<Long> programIdList = programList.stream().map(Program::getId).collect(Collectors.toList());
        LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper = Wrappers.lambdaQuery(ProgramShowTime.class)
                .in(ProgramShowTime::getProgramId, programIdList);
        List<ProgramShowTime> programShowTimeList = programShowTimeMapper.selectList(programShowTimeLambdaQueryWrapper);
        Map<Long, List<ProgramShowTime>> programShowTimeMap = 
                programShowTimeList.stream().collect(Collectors.groupingBy(ProgramShowTime::getProgramId));
        
        Map<Long, TicketCategoryAggregate> ticketCategorieMap = selectTicketCategorieMap(programIdList);
        
        Map<Long, List<Program>> programMap = programList.stream()
                .collect(Collectors.groupingBy(Program::getParentProgramCategoryId));
        
        for (Entry<Long, List<Program>> programEntry : programMap.entrySet()) {
            Long key = programEntry.getKey();
            List<Program> value = programEntry.getValue();
            List<ProgramListVo> programListVoList = new ArrayList<>();
            for (Program program : value) {
                ProgramListVo programListVo = new ProgramListVo();
                BeanUtil.copyProperties(program,programListVo);
                
                programListVo.setShowTime(Optional.ofNullable(programShowTimeMap.get(program.getId()))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .map(ProgramShowTime::getShowTime)
                        .orElse(null));
                programListVo.setShowDayTime(Optional.ofNullable(programShowTimeMap.get(program.getId()))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .map(ProgramShowTime::getShowDayTime)
                        .orElse(null));
                programListVo.setShowWeekTime(Optional.ofNullable(programShowTimeMap.get(program.getId()))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .map(ProgramShowTime::getShowWeekTime)
                        .orElse(null));
                
                programListVo.setMaxPrice(Optional.ofNullable(ticketCategorieMap.get(program.getId()))
                        .map(TicketCategoryAggregate::getMaxPrice).orElse(null));
                programListVo.setMinPrice(Optional.ofNullable(ticketCategorieMap.get(program.getId()))
                        .map(TicketCategoryAggregate::getMinPrice).orElse(null));
                programListVoList.add(programListVo);
            }
            ProgramHomeVo programHomeVo = new ProgramHomeVo();
            programHomeVo.setCategoryName(programCategoryMap.get(key));
            programHomeVo.setCategoryId(key);
            programHomeVo.setProgramListVoList(programListVoList);
            programHomeVoList.add(programHomeVo);
        }
        return programHomeVoList;
    }
    
    /**
     * 组装节目参数
     * @param programPageListDto 节目数据的入参
     * */
    public void setQueryTime(ProgramPageListDto programPageListDto){
        switch (programPageListDto.getTimeType()) {
            case ProgramTimeType.TODAY:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.now(FORMAT_DATE));
                break;
            case ProgramTimeType.TOMORROW:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.addDay(DateUtils.now(FORMAT_DATE),1));
                break;
            case ProgramTimeType.WEEK:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.addWeek(DateUtils.now(FORMAT_DATE),1));
                break;
            case ProgramTimeType.MONTH:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.addMonth(DateUtils.now(FORMAT_DATE),1));
                break;
            case ProgramTimeType.CALENDAR:
                if (Objects.isNull(programPageListDto.getStartDateTime())) {
                    throw new TicketStormFrameException(BaseCode.START_DATE_TIME_NOT_EXIST);
                }
                if (Objects.isNull(programPageListDto.getEndDateTime())) {
                    throw new TicketStormFrameException(BaseCode.END_DATE_TIME_NOT_EXIST);
                }
                break;
            default:
                programPageListDto.setStartDateTime(null);
                programPageListDto.setEndDateTime(null);
                break;
        }
    }
    
    /**
     * 查询分类列表（数据库查询）
     * @param programPageListDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public PageVo<ProgramListVo> selectPage(ProgramPageListDto programPageListDto) {
        setQueryTime(programPageListDto);
        PageVo<ProgramListVo> pageVo = programEs.selectPage(programPageListDto);
        if (CollectionUtil.isNotEmpty(pageVo.getList())) {
            return pageVo;
        }
        return dbSelectPage(programPageListDto);
    }
    
    /**
     * 推荐列表
     * @param programRecommendListDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public List<ProgramListVo> recommendList(ProgramRecommendListDto programRecommendListDto){
        compositeContainer.execute(CompositeCheckType.PROGRAM_RECOMMEND_CHECK.getValue(),programRecommendListDto);
        return programEs.recommendList(programRecommendListDto);
    }
    
    /**
     * 查询分类信息（数据库查询）
     * @param programPageListDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public PageVo<ProgramListVo> dbSelectPage(ProgramPageListDto programPageListDto) {
        IPage<ProgramJoinShowTime> iPage = 
                programMapper.selectPage(PageUtil.getPageParams(programPageListDto), programPageListDto);
        if (CollectionUtil.isEmpty(iPage.getRecords())) {
            return new PageVo<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), new ArrayList<>());
        }
        Set<Long> programCategoryIdList = 
                iPage.getRecords().stream().map(Program::getProgramCategoryId).collect(Collectors.toSet());
        Map<Long, String> programCategoryMap = selectProgramCategoryMap(programCategoryIdList);
        
        List<Long> programIdList = iPage.getRecords().stream().map(Program::getId).collect(Collectors.toList());
        Map<Long, TicketCategoryAggregate> ticketCategorieMap = selectTicketCategorieMap(programIdList);
        
        Map<Long,String> tempAreaMap = new HashMap<>(64);
        AreaSelectDto areaSelectDto = new AreaSelectDto();
        areaSelectDto.setIdList(iPage.getRecords().stream().map(Program::getAreaId).distinct().collect(Collectors.toList()));
        ApiResponse<List<AreaVo>> areaResponse = baseDataClient.selectByIdList(areaSelectDto);
        if (Objects.equals(areaResponse.getCode(), ApiResponse.ok().getCode())) {
            if (CollectionUtil.isNotEmpty(areaResponse.getData())) {
                tempAreaMap = areaResponse.getData().stream()
                        .collect(Collectors.toMap(AreaVo::getId,AreaVo::getName,(v1,v2) -> v2));
            }
        }else {
            log.error("base-data selectByIdList rpc error areaResponse:{}", JSON.toJSONString(areaResponse));
        }
        Map<Long,String> areaMap = tempAreaMap;
        
        return PageUtil.convertPage(iPage, programJoinShowTime -> {
            ProgramListVo programListVo = new ProgramListVo();
            BeanUtil.copyProperties(programJoinShowTime, programListVo);
            
            programListVo.setAreaName(areaMap.get(programJoinShowTime.getAreaId()));
            programListVo.setProgramCategoryName(programCategoryMap.get(programJoinShowTime.getProgramCategoryId()));
            programListVo.setMinPrice(Optional.ofNullable(ticketCategorieMap.get(programJoinShowTime.getId()))
                    .map(TicketCategoryAggregate::getMinPrice).orElse(null));
            programListVo.setMaxPrice(Optional.ofNullable(ticketCategorieMap.get(programJoinShowTime.getId()))
                    .map(TicketCategoryAggregate::getMaxPrice).orElse(null));
            return programListVo;
        });
    }
    
    /**
     * 查询节目详情
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public ProgramVo detail(ProgramGetDto programGetDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue(),programGetDto);
        return getDetail(programGetDto);
    }
    
    /**
     * 查询节目详情V1
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public ProgramVo detailV1(ProgramGetDto programGetDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue(),programGetDto);
        return getDetail(programGetDto);
    }
    
    /**
     * 查询节目详情V2
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public ProgramVo detailV2(ProgramGetDto programGetDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue(),programGetDto);
        return getDetailV2(programGetDto);
    }
    
    /**
     * 查询节目详情执行
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public ProgramVo getDetail(ProgramGetDto programGetDto) {
        ProgramShowTime programShowTime = programShowTimeService.selectProgramShowTimeByProgramId(programGetDto.getId());
        ProgramVo programVo = programService.getById(programGetDto.getId(),DateUtils.countBetweenSecond(DateUtils.now(),
                programShowTime.getShowTime()), TimeUnit.SECONDS);
        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());
        
        ProgramGroupVo programGroupVo = programService.getProgramGroup(programVo.getProgramGroupId());
        programVo.setProgramGroupVo(programGroupVo);
        
        preloadTicketUserList(programVo.getHighHeat());
        
        preloadAccountOrderCount(programVo.getId());
        
        ProgramCategory programCategory = getProgramCategory(programVo.getProgramCategoryId());
        if (Objects.nonNull(programCategory)) {
            programVo.setProgramCategoryName(programCategory.getName());
        }
        ProgramCategory parentProgramCategory = getProgramCategory(programVo.getParentProgramCategoryId());
        if (Objects.nonNull(parentProgramCategory)) {
            programVo.setParentProgramCategoryName(parentProgramCategory.getName());
        }
        
        List<TicketCategoryVo> ticketCategoryVoList =
                ticketCategoryService.selectTicketCategoryListByProgramId(programVo.getId(),
                        DateUtils.countBetweenSecond(DateUtils.now(),programShowTime.getShowTime()), TimeUnit.SECONDS);
        programVo.setTicketCategoryVoList(ticketCategoryVoList);
        
        return programVo;
    }
    
    /**
     * 查询节目详情V2执行
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     * */
    public ProgramVo getDetailV2(ProgramGetDto programGetDto) {
        ProgramShowTime programShowTime =
                programShowTimeService.selectProgramShowTimeByProgramIdMultipleCache(programGetDto.getId());
        
        ProgramVo programVo = programService.getByIdMultipleCache(programGetDto.getId(),programShowTime.getShowTime());
        
        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());
        
        ProgramGroupVo programGroupVo = programService.getProgramGroupMultipleCache(programVo.getProgramGroupId());
        programVo.setProgramGroupVo(programGroupVo);
        
        preloadTicketUserList(programVo.getHighHeat());
        
        preloadAccountOrderCount(programVo.getId());
        
        ProgramCategory programCategory = getProgramCategoryMultipleCache(programVo.getProgramCategoryId());
        if (Objects.nonNull(programCategory)) {
            programVo.setProgramCategoryName(programCategory.getName());
        }
        ProgramCategory parentProgramCategory = getProgramCategoryMultipleCache(programVo.getParentProgramCategoryId());
        if (Objects.nonNull(parentProgramCategory)) {
            programVo.setParentProgramCategoryName(parentProgramCategory.getName());
        }
        
        List<TicketCategoryVo> ticketCategoryVoList = ticketCategoryService
                .selectTicketCategoryListByProgramIdMultipleCache(programVo.getId(),programShowTime.getShowTime());
        programVo.setTicketCategoryVoList(ticketCategoryVoList);
        
        return programVo;
    }
    
    /**
     * 查询节目表详情执行（多级）
     * @param programId 节目id
     * @param showTime 节目演出时间
     * @return 执行后的结果
     * */
    public ProgramVo getByIdMultipleCache(Long programId, Date showTime){
        return localCacheProgram.getCache(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId).getRelKey(),
                key -> {
                    log.info("查询节目详情 从本地缓存没有查询到 节目id : {}",programId);
                    ProgramVo programVo = getById(programId,DateUtils.countBetweenSecond(DateUtils.now(),showTime),
                            TimeUnit.SECONDS);
                    programVo.setShowTime(showTime);
                    return programVo;
                });
    }
    
    public ProgramVo simpleGetByIdMultipleCache(Long programId){
        ProgramVo programVoCache = localCacheProgram.getCache(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, 
                programId).getRelKey());
        if (Objects.nonNull(programVoCache)) {
            return programVoCache;
        }
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId), ProgramVo.class);
    }
    
    public ProgramVo simpleGetProgramAndShowMultipleCache(Long programId){
        ProgramShowTime programShowTime =
                programShowTimeService.simpleSelectProgramShowTimeByProgramIdMultipleCache(programId);
        if (Objects.isNull(programShowTime)) {
            throw new TicketStormFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST);
        }
        
        ProgramVo programVo = simpleGetByIdMultipleCache(programId);
        if (Objects.isNull(programVo)) {
            throw new TicketStormFrameException(BaseCode.PROGRAM_NOT_EXIST);
        }
        
        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());
        
        return programVo;
    }
    
    @ServiceLock(lockType= LockType.Read,name = PROGRAM_LOCK,keys = {"#programId"})
    public ProgramVo getById(Long programId,Long expireTime,TimeUnit timeUnit) {
        ProgramVo programVo = 
                redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId), ProgramVo.class);
        if (Objects.nonNull(programVo)) {
            return programVo;
        }
        log.info("查询节目详情 从Redis缓存没有查询到 节目id : {}",programId);
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_LOCK, new String[]{String.valueOf(programId)});
        lock.lock();
        try {
            return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM,programId)
                    ,ProgramVo.class,
                    () -> createProgramVo(programId)
                    ,expireTime,
                    timeUnit);
        }finally {
            lock.unlock();
        }
    }
    
    public ProgramGroupVo getProgramGroupMultipleCache(Long programGroupId){
        return localCacheProgramGroup.getCache(
                RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId).getRelKey(),
                key -> getProgramGroup(programGroupId));
    }
    @ServiceLock(lockType= LockType.Read,name = PROGRAM_GROUP_LOCK,keys = {"#programGroupId"})
    public ProgramGroupVo getProgramGroup(Long programGroupId) {
        ProgramGroupVo programGroupVo =
                redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId), ProgramGroupVo.class);
        if (Objects.nonNull(programGroupVo)) {
            return programGroupVo;
        }
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_LOCK, new String[]{String.valueOf(programGroupId)});
        lock.lock();
        try {
            programGroupVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId), 
                    ProgramGroupVo.class);
            if (Objects.isNull(programGroupVo)) {
                programGroupVo = createProgramGroupVo(programGroupId);
                redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId),programGroupVo,
                        DateUtils.countBetweenSecond(DateUtils.now(),programGroupVo.getRecentShowTime()),TimeUnit.SECONDS);
            }
            return programGroupVo;
        }finally {
            lock.unlock();
        }
    }
    
    public Map<Long, String> selectProgramCategoryMap(Collection<Long> programCategoryIdList){
        LambdaQueryWrapper<ProgramCategory> pcLambdaQueryWrapper = Wrappers.lambdaQuery(ProgramCategory.class)
                .in(ProgramCategory::getId, programCategoryIdList);
        List<ProgramCategory> programCategoryList = programCategoryMapper.selectList(pcLambdaQueryWrapper);
        return programCategoryList
                .stream()
                .collect(Collectors.toMap(ProgramCategory::getId, ProgramCategory::getName, (v1, v2) -> v2));
    }
    
    public Map<Long, TicketCategoryAggregate> selectTicketCategorieMap(List<Long> programIdList){
        List<TicketCategoryAggregate> ticketCategorieList = ticketCategoryMapper.selectAggregateList(programIdList);
        return ticketCategorieList
                .stream()
                .collect(Collectors.toMap(TicketCategoryAggregate::getProgramId, 
                        ticketCategory -> ticketCategory, (v1, v2) -> v2));
    }
    
    @RepeatExecuteLimit(name = CANCEL_PROGRAM_ORDER,keys = {"#programOperateDataDto.programId","#programOperateDataDto.seatIdList"})
    @Transactional(rollbackFor = Exception.class)
    public void operateProgramData(ProgramOperateDataDto programOperateDataDto){
        List<TicketCategoryCountDto> ticketCategoryCountDtoList = programOperateDataDto.getTicketCategoryCountDtoList();
        List<Long> seatIdList = programOperateDataDto.getSeatIdList();
        LambdaQueryWrapper<Seat> seatLambdaQueryWrapper = 
                Wrappers.lambdaQuery(Seat.class)
                        .eq(Seat::getProgramId,programOperateDataDto.getProgramId())
                        .in(Seat::getId, seatIdList);
        List<Seat> seatList = seatMapper.selectList(seatLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(seatList)) {
            throw new TicketStormFrameException(BaseCode.SEAT_NOT_EXIST);
        }
        if (seatList.size() != seatIdList.size()) {
            throw new TicketStormFrameException(BaseCode.SEAT_UPDATE_REL_COUNT_NOT_EQUAL_PRESET_COUNT);
        }
        for (Seat seat : seatList) {
            if (Objects.equals(seat.getSellStatus(), SellStatus.SOLD.getCode())) {
                throw new TicketStormFrameException(BaseCode.SEAT_SOLD);
            }
        }
        LambdaUpdateWrapper<Seat> seatLambdaUpdateWrapper = 
                Wrappers.lambdaUpdate(Seat.class)
                        .eq(Seat::getProgramId,programOperateDataDto.getProgramId())
                        .in(Seat::getId, seatIdList);
        Seat updateSeat = new Seat();
        updateSeat.setSellStatus(SellStatus.SOLD.getCode());
        seatMapper.update(updateSeat,seatLambdaUpdateWrapper);
        
        int updateRemainNumberCount = 
                ticketCategoryMapper.batchUpdateRemainNumber(ticketCategoryCountDtoList,programOperateDataDto.getProgramId());
        if (updateRemainNumberCount != ticketCategoryCountDtoList.size()) {
            throw new TicketStormFrameException(BaseCode.UPDATE_TICKET_CATEGORY_COUNT_NOT_CORRECT);
        }
    }
    
    private ProgramVo createProgramVo(Long programId){
        ProgramVo programVo = new ProgramVo();
        Program program = 
                Optional.ofNullable(programMapper.selectById(programId))
                        .orElseThrow(() -> new TicketStormFrameException(BaseCode.PROGRAM_NOT_EXIST));
        BeanUtil.copyProperties(program,programVo);
        AreaGetDto areaGetDto = new AreaGetDto();
        areaGetDto.setId(program.getAreaId());
        ApiResponse<AreaVo> areaResponse = baseDataClient.getById(areaGetDto);
        if (Objects.equals(areaResponse.getCode(), ApiResponse.ok().getCode())) {
            if (Objects.nonNull(areaResponse.getData())) {
                programVo.setAreaName(areaResponse.getData().getName());
            }
        }else {
            log.error("base-data rpc getById error areaResponse:{}", JSON.toJSONString(areaResponse));
        }
        return programVo;
    }
    
    private ProgramGroupVo createProgramGroupVo(Long programGroupId){
        ProgramGroupVo programGroupVo = new ProgramGroupVo();
        ProgramGroup programGroup =
                Optional.ofNullable(programGroupMapper.selectById(programGroupId))
                        .orElseThrow(() -> new TicketStormFrameException(BaseCode.PROGRAM_GROUP_NOT_EXIST));
        programGroupVo.setId(programGroup.getId());
        programGroupVo.setProgramSimpleInfoVoList(JSON.parseArray(programGroup.getProgramJson(), ProgramSimpleInfoVo.class));
        programGroupVo.setRecentShowTime(programGroup.getRecentShowTime());
        return programGroupVo;
    }
    
    public List<Long> getAllProgramIdList(){
        LambdaQueryWrapper<Program> programLambdaQueryWrapper =
                Wrappers.lambdaQuery(Program.class).eq(Program::getProgramStatus, BusinessStatus.YES.getCode())
                        .select(Program::getId);
        List<Program> programs = programMapper.selectList(programLambdaQueryWrapper);
        return programs.stream().map(Program::getId).collect(Collectors.toList());
    }
    
    public ProgramVo getDetailFromDb(Long programId) {
        ProgramVo programVo = createProgramVo(programId);
        
        ProgramCategory programCategory = getProgramCategory(programVo.getProgramCategoryId());
        if (Objects.nonNull(programCategory)) {
            programVo.setProgramCategoryName(programCategory.getName());
        }
        ProgramCategory parentProgramCategory = getProgramCategory(programVo.getParentProgramCategoryId());
        if (Objects.nonNull(parentProgramCategory)) {
            programVo.setParentProgramCategoryName(parentProgramCategory.getName());
        }
        
        LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper =
                Wrappers.lambdaQuery(ProgramShowTime.class).eq(ProgramShowTime::getProgramId, programId);
        ProgramShowTime programShowTime = Optional.ofNullable(programShowTimeMapper.selectOne(programShowTimeLambdaQueryWrapper))
                .orElseThrow(() -> new TicketStormFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST));
        
        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());
        
        return programVo;
    }
    
    private void preloadTicketUserList(Integer highHeat){
        if (Objects.equals(highHeat, BusinessStatus.NO.getCode())) {
            return;
        }
        String userId = BaseParameterHolder.getParameter(USER_ID);
        String code = BaseParameterHolder.getParameter(CODE);
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(code)) {
            return;
        }
        Boolean userLogin =
                redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, userId));
        if (!userLogin) {
            return;
        }
        BusinessThreadPool.execute(() -> {
            try {
                if (!redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.TICKET_USER_LIST,userId))) {
                    TicketUserListDto ticketUserListDto = new TicketUserListDto();
                    ticketUserListDto.setUserId(Long.parseLong(userId));
                    ApiResponse<List<TicketUserVo>> apiResponse = userClient.list(ticketUserListDto);
                    if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                        Optional.ofNullable(apiResponse.getData()).filter(CollectionUtil::isNotEmpty)
                                .ifPresent(ticketUserVoList -> redisCache.set(RedisKeyBuild.createRedisKey(
                                        RedisKeyManage.TICKET_USER_LIST,userId),ticketUserVoList));
                    }else {
                        log.warn("userClient.select 调用失败 apiResponse : {}",JSON.toJSONString(apiResponse));
                    }
                }
                
            }catch (Exception e) {
                log.error("预热加载购票人列表失败",e);
            }
        });
    }
    
    private void preloadAccountOrderCount(Long programId){
        String userId = BaseParameterHolder.getParameter(USER_ID);
        String code = BaseParameterHolder.getParameter(CODE);
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(code)) {
            return;
        }
        Boolean userLogin =
                redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, userId));
        if (!userLogin) {
            return;
        }
        BusinessThreadPool.execute(() -> {
            try {
                if (!redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,userId,programId))) {
                    AccountOrderCountDto accountOrderCountDto = new AccountOrderCountDto();
                    accountOrderCountDto.setUserId(Long.parseLong(userId));
                    accountOrderCountDto.setProgramId(programId);
                    ApiResponse<AccountOrderCountVo> apiResponse = orderClient.accountOrderCount(accountOrderCountDto);
                    if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                        Optional.ofNullable(apiResponse.getData())
                                .ifPresent(accountOrderCountVo -> redisCache.set(
                                        RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,userId,programId),
                                        accountOrderCountVo.getCount(), tokenExpireManager.getTokenExpireTime() + 1,
                                        TimeUnit.MINUTES));
                    }else {
                        log.warn("orderClient.accountOrderCount 调用失败 apiResponse : {}",JSON.toJSONString(apiResponse));
                    }
                }
            }catch (Exception e) {
                log.error("预热加载账户订单数量失败",e);
            }
        });
    }
    
    public ProgramCategory getProgramCategoryMultipleCache(Long programCategoryId){
        return localCacheProgramCategory.get(String.valueOf(programCategoryId),
                key -> getProgramCategory(programCategoryId));
    }
    
    public ProgramCategory getProgramCategory(Long programCategoryId){
        return programCategoryService.getProgramCategory(programCategoryId);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetExecute(ProgramResetExecuteDto programResetExecuteDto) {
        Long programId = programResetExecuteDto.getProgramId();
        LambdaQueryWrapper<Seat> seatQueryWrapper =
                Wrappers.lambdaQuery(Seat.class).eq(Seat::getProgramId, programId)
                        .in(Seat::getSellStatus,SellStatus.LOCK.getCode(),SellStatus.SOLD.getCode());
        List<Seat> seatList = seatMapper.selectList(seatQueryWrapper);
        if (CollectionUtil.isNotEmpty(seatList)) {
            LambdaUpdateWrapper<Seat> seatUpdateWrapper =
                    Wrappers.lambdaUpdate(Seat.class).eq(Seat::getProgramId, programId);
            Seat seatUpdate = new Seat();
            seatUpdate.setSellStatus(SellStatus.NO_SOLD.getCode());
            seatMapper.update(seatUpdate,seatUpdateWrapper);
        }
        LambdaQueryWrapper<TicketCategory> ticketCategoryQueryWrapper =
                Wrappers.lambdaQuery(TicketCategory.class).eq(TicketCategory::getProgramId, programId);
        List<TicketCategory> ticketCategories = ticketCategoryMapper.selectList(ticketCategoryQueryWrapper);
        if (CollectionUtil.isNotEmpty(ticketCategories)) {
            for (TicketCategory ticketCategory : ticketCategories) {
                Long remainNumber = ticketCategory.getRemainNumber();
                Long totalNumber = ticketCategory.getTotalNumber();
                if (!(remainNumber.equals(totalNumber))) {
                    TicketCategory ticketCategoryUpdate = new TicketCategory();
                    ticketCategoryUpdate.setRemainNumber(totalNumber);
                    
                    LambdaUpdateWrapper<TicketCategory> ticketCategoryUpdateWrapper =
                            Wrappers.lambdaUpdate(TicketCategory.class)
                                    .eq(TicketCategory::getProgramId, programId)
                                    .eq(TicketCategory::getId,ticketCategory.getId());
                    ticketCategoryMapper.update(ticketCategoryUpdate,ticketCategoryUpdateWrapper);
                }
            }
        }
        delRedisData(programId);
        delLocalCache(programId);
        return true;
    }
    
    public void delRedisData(Long programId){
        Program program = Optional.ofNullable(programMapper.selectById(programId))
                .orElseThrow(() -> new TicketStormFrameException(BaseCode.PROGRAM_NOT_EXIST));
        List<String> keys = new ArrayList<>();
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM,programId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP,program.getProgramGroupId()).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME,programId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId,"*").getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH, programId,"*").getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH, programId,"*").getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_CATEGORY_LIST, programId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId,"*").getRelKey());
        programDelCacheData.del(keys,new String[]{});
    }
    
    public Boolean invalid(final ProgramInvalidDto programInvalidDto) {
        Program program = new Program();
        program.setId(programInvalidDto.getId());
        program.setProgramStatus(BusinessStatus.NO.getCode());
        int result = programMapper.updateById(program);
        if (result > 0) {
            delRedisData(programInvalidDto.getId());
            redisStreamPushHandler.push(String.valueOf(programInvalidDto.getId()));
            programEs.deleteByProgramId(programInvalidDto.getId());
            return true;
        }else {
            return false;
        }
    }
    
    public ProgramVo localDetail(final ProgramGetDto programGetDto) {
        return localCacheProgram.getCache(String.valueOf(programGetDto.getId()));
    }
    
    public void delLocalCache(Long programId){
        log.info("删除本地缓存 programId : {}",programId);
        localCacheProgram.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId).getRelKey());
        localCacheProgramGroup.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programId).getRelKey());
        localCacheProgramShowTime.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey());
        localCacheTicketCategory.del(programId);
    }
}

