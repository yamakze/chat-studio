package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.infrastructure.dao.po.AiClientAdvisorConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户端-顾问关联表DAO
 */
@Mapper
public interface AiClientAdvisorConfigDao extends BaseMapper<AiClientAdvisorConfig> {

    @Select("""
                <script>
                    SELECT *
                    FROM ai_client_advisor_config
                    WHERE client_id IN
                    <foreach collection="clientIdList" item="id" open="(" separator="," close=")">
                        #{id}
                    </foreach>
                </script>
            """)
    List<AiClientAdvisorConfig> queryClientAdvisorConfigByClientIds(@Param("clientIdList") List<Long> clientIdList);

    @Select("""
                <script>
                    SELECT advisor_id
                    FROM ai_client_advisor_config
                    WHERE client_id IN
                    <foreach collection="clientIdList" item="id" open="(" separator="," close=")">
                        #{id}
                    </foreach>
                </script>
            """)
    List<Long> queryAdvisorIdsByClientIds(@Param("clientIdList") List<Long> clientIdList);

    @Select("SELECT client_id FROM ai_client_advisor_config WHERE advisor_id = #{advisorId}")
    List<Long> queryClientIdsByAdvisorId(@Param("advisorId") Long advisorId);


    @Delete("DELETE  FROM ai_client_advisor_config WHERE client_id = #{clientId}")
    int deleteBatchByClientId(@Param("clientId") Long clientId);

    @Insert({
            "<script>",
            "INSERT INTO ai_client_advisor_config (client_id, advisor_id, create_time) VALUES",
            "<foreach collection=\"advisorIds\" item=\"advisorId\" separator=\",\">",
            "(#{clientId}, #{advisorId}, NOW())",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("clientId") Long clientId, @Param("advisorIds") List<Long> advisorIds);
}
