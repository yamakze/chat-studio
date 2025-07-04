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
    @Select("<script> " +
            "SELECT model_id FROM ai_client " +
            "WHERE id IN " +
            "<foreach item='item' collection='clientIdList' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<Long> queryModelIdsByClientIds(@Param("clientIdList") List<Long> clientIdList);

    @Select("<script> " +
            "SELECT system_prompt_id FROM ai_client WHERE id IN " +
            "<foreach item='item' collection='clientIdList' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<Long> queryPromptIdsByClientIds(@Param("clientIdList") List<Long> clientIdList);

    @Select("SELECT * FROM ai_client WHERE model_id = #{modelId}")
    List<AiClient> queryAiClientsByModelId(Long modelId);

    @Select("SELECT id FROM ai_client WHERE status = 1")
    List<Long> queryValidClientIds();

    @Select("SELECT id FROM ai_client")
    List<Long> queryAllClientIds();

    @Select("SELECT id FROM ai_client WHERE system_prompt_id = #{promptId}")
    List<AiClient> queryAiClientSByPromptId(@NonNull Long promptId);
}
