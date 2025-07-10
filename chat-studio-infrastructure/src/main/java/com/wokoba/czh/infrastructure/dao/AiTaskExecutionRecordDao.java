package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiTaskExecutionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiTaskExecutionRecordDao extends BaseMapper<AiTaskExecutionRecord> {
}