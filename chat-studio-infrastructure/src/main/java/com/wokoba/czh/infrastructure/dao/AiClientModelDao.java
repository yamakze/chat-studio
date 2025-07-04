package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiClientModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI模型配置数据访问接口
 */
@Mapper
public interface AiClientModelDao extends BaseMapper<AiClientModel> {}
