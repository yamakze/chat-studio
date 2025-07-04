package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiClientSystemPrompt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统提示词配置数据访问接口
 */
@Mapper
public interface AiClientSystemPromptDao extends BaseMapper<AiClientSystemPrompt> {

}