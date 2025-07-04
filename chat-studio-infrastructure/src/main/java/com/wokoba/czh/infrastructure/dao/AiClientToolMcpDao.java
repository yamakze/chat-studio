package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.infrastructure.dao.po.AiClientToolMcp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MCP客户端配置数据访问接口
 */
@Mapper
public interface AiClientToolMcpDao extends BaseMapper<AiClientToolMcp> {

    default List<AiClientToolMcp> queryAiClientToolMcpByMcpIds(List<Long> mcpIds) {
        if (mcpIds == null || mcpIds.isEmpty()) return List.of();
        return selectList(Wrappers.<AiClientToolMcp>lambdaQuery()
                .in(AiClientToolMcp::getId, mcpIds)
                .eq(AiClientToolMcp::getStatus, 1)
                .orderByAsc(AiClientToolMcp::getCreateTime));
    }


    default List<Long> queryBasicToolIds() {
        return selectList(Wrappers.<AiClientToolMcp>lambdaQuery()
                .eq(AiClientToolMcp::getBasic, 1)
                .eq(AiClientToolMcp::getStatus, 1))
                .stream().map(AiClientToolMcp::getId).toList();
    }
}