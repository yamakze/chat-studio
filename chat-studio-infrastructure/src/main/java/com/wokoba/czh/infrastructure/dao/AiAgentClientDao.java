package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiAgentClient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能体-客户端关联表DAO接口
 */
@Mapper
public interface AiAgentClientDao extends BaseMapper<AiAgentClient> {

}