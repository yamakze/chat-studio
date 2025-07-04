package com.wokoba.czh.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.domain.agent.model.entity.AiClientModelEntity;
import com.wokoba.czh.infrastructure.dao.po.AiClientModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatModelDao extends BaseMapper<AiClientModel> {

    @Select("""
        <script>
            SELECT
                id,
                model_name AS modelName,
                base_url AS baseUrl,
                api_key AS apiKey,
                completions_path AS completionsPath,
                model_type AS modelType,
                model_version AS modelVersion,
                timeout
            FROM ai_client_model
            WHERE status = 1 AND  id IN
            <foreach collection="modelIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
        </script>
    """)
    List<AiClientModelEntity> queryAiClientModelEntityByIds(@Param("modelIds") List<Long> modelIds);


    @Insert("""
            <script>
                INSERT INTO ai_client_model (model_name, base_url, api_key, completions_path, model_type, model_version, timeout)
                VALUES
                <foreach collection="modelList" item="modelVersion" separator=",">
                    (#{aiClientModel.modelName}, #{aiClientModel.baseUrl}, #{aiClientModel.apiKey}, #{aiClientModel.completionsPath}, #{aiClientModel.modelType}, #{modelVersion}, #{aiClientModel.timeout})
                </foreach>
            </script>
        """)
    void insertBatch(@Param("aiClientModel") AiClientModel aiClientModel, @Param("modelList") List<String> modelList);

}

