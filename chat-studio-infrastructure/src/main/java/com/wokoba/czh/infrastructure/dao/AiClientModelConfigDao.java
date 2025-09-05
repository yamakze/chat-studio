package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiClientModelConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiClientModelConfigDao extends BaseMapper<AiClientModelConfig> {
    @Insert("INSERT INTO ai_client_model_config (client_id, model_id, model_version, options) " +
            "VALUES (#{clientId}, #{modelId}, #{modelVersion}, #{options}) " +
            "ON DUPLICATE KEY UPDATE " +
            "id = LAST_INSERT_ID(id), " +
            "model_version = VALUES(model_version), " +
            "options = VALUES(options)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int upsertByClientIdAndModelIdAndVersion(AiClientModelConfig config);

    @Select("select client_id from ai_client_model_config where model_id = #{modelId}")
    List<Long> queryAiClientIdsByModelId(Long modelId);
}
