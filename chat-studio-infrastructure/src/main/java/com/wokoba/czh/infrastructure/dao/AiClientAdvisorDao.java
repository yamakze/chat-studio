package com.wokoba.czh.infrastructure.dao;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.domain.agent.model.entity.AiClientAdvisorEntity;
import com.wokoba.czh.infrastructure.dao.po.AiClientAdvisor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 顾问配置数据访问接口
 */
@Mapper
public interface AiClientAdvisorDao extends BaseMapper<AiClientAdvisor> {

    default List<AiClientAdvisorEntity> queryAdvisorEntityByIds(List<Long> advisorIds) {
        if (advisorIds == null || advisorIds.isEmpty()) return List.of();
        List<AiClientAdvisor> aiClientAdvisors = selectList(Wrappers.<AiClientAdvisor>lambdaQuery()
                .in(AiClientAdvisor::getId, advisorIds)
                .eq(AiClientAdvisor::getStatus, 1)
                .orderByDesc(AiClientAdvisor::getCreateTime));
        return aiClientAdvisors.stream().map(aiClientAdvisor -> AiClientAdvisorEntity.builder()
                .id(aiClientAdvisor.getId())
                .advisorName(aiClientAdvisor.getAdvisorName())
                .advisorType(aiClientAdvisor.getAdvisorType())
                .extraParams(JSON.parseObject(aiClientAdvisor.getExtParam()))
                .build()).toList();
    }

    default List<Long> queryBasicAdvisorIds() {
        return selectList(Wrappers.<AiClientAdvisor>lambdaQuery()
                .eq(AiClientAdvisor::getBasic, 1)
                .eq(AiClientAdvisor::getStatus, 1))
                .stream().map(AiClientAdvisor::getId).toList();
    }

    ;
}
