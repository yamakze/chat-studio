package com.wokoba.czh.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.infrastructure.dao.po.AiAgentFlowConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiAgentFlowConfigDao extends BaseMapper<AiAgentFlowConfig> {
   default List<AiAgentFlowConfig> queryByAgentId(String aiAgentId){
       return selectList(Wrappers.lambdaQuery(AiAgentFlowConfig.class)
               .eq(AiAgentFlowConfig::getAgentId,aiAgentId));
   }
}
