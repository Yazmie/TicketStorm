package com.ticketstorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticketstorm.dto.ApiDataDto;
import com.ticketstorm.entity.ApiData;
import com.ticketstorm.vo.ApiDataVo;


public interface ApiDataMapper extends BaseMapper<ApiData> {
    /**
     * 分页查询
     * @param page 分页对象
     * @param apiDataDto 参数
     * @return 分页数据
     * */
    Page<ApiDataVo> pageList(Page<ApiData> page, ApiDataDto apiDataDto);
}
