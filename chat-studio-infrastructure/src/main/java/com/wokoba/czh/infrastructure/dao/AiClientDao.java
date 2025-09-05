package com.wokoba.czh.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiClient;
import lombok.NonNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiClientDao extends BaseMapper<AiClient> {

    @Select("""
            <script> 
            SELECT DISTINCT acmc.model_id
            FROM ai_client ac
            JOIN ai_client_model_config acmc ON ac.model_config_id = acmc.id
            WHERE ac.id IN
            <foreach collection="clientIdList" item="clientId" open="(" separator="," close=")">
                #{clientId}
            </foreach>
            </script>
            """)
    List<Long> queryModelIdsByClientIds(@Param("clientIdList") List<Long> clientIdList);

    @Select("<script> " +
            "SELECT system_prompt_id FROM ai_client WHERE id IN " +
            "<foreach item='item' collection='clientIdList' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<Long> queryPromptIdsByClientIds(@Param("clientIdList") List<Long> clientIdList);



    @Select("SELECT id FROM ai_client WHERE status = 1")
    List<Long> queryValidClientIds();

    @Select("SELECT id FROM ai_client")
    List<Long> queryAllClientIds();

    @Select("SELECT id FROM ai_client WHERE system_prompt_id = #{promptId}")
    List<AiClient> queryAiClientSByPromptId(@NonNull Long promptId);

    /**
     * 统计指定 modelId 正在被 client 使用的数量
     * @param modelId 模型 ID
     * @return 正在使用该模型的客户端数量
     */
    @Select("""
        SELECT COUNT(c.id)
        FROM ai_client c
        JOIN ai_client_model_config m
            ON c.model_config_id = m.id
        WHERE m.model_id = #{modelId}
        """)
    int countByModelInUse(@Param("modelId") Long modelId);
}
