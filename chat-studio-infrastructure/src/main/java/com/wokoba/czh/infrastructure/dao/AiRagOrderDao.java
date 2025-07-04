package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiClientToolConfig;
import com.wokoba.czh.infrastructure.dao.po.AiRagOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库配置数据访问接口
 */
@Mapper
public interface AiRagOrderDao extends BaseMapper<AiRagOrder> {

}