package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiClientToolConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 客户端工具配置DAO接口
 */
@Mapper
public interface AiClientToolConfigDao extends BaseMapper<AiClientToolConfig> {

    @Select("""
                <script>
                    SELECT tool_id
                    FROM ai_client_tool_config
                    WHERE client_id IN
                    <foreach collection="clientIdList" item="id" open="(" separator="," close=")">
                        #{id}
                    </foreach>
                </script>
            """)
    List<Long> queryMcpIdsByClientIds(@Param("clientIdList") List<Long> clientIdList);

    @Select("""
                <script>
                    SELECT *
                    FROM ai_client_tool_config
                    WHERE client_id IN
                    <foreach collection="clientIdList" item="id" open="(" separator="," close=")">
                        #{id}
                    </foreach>
                </script>
            """)
    List<AiClientToolConfig> queryToolConfigByClientIds(@Param("clientIdList") List<Long> clientIdList);


    @Delete("""
                DELETE FROM ai_client_tool_config
                WHERE client_id = #{clientId}
            """)
    void deleteBatchByClientId(Long clientId);

    @Insert("""
                <script>
                INSERT INTO ai_client_tool_config (client_id, tool_type, tool_id, create_time)
                VALUES
                <foreach collection="mcpIds" item="mcpId" separator=",">
                    (#{clientId}, 'mcp', #{mcpId}, NOW())
                </foreach>
                </script>
            """)
    void insertBatch(Long clientId, List<Long> mcpIds);

    @Select("SELECT client_id FROM ai_client_tool_config WHERE tool_id = #{tool_id}")
    List<Long> queryClientIdsByToolId(Long toolId);
}
