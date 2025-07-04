package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.AiAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI智能体配置数据访问接口
 */
@Mapper
public interface AiAgentDao extends BaseMapper<AiAgent> {

    @Select("""
              SELECT DISTINCT\s
                        ac.client_id
                    FROM\s
                        ai_agent a
                    JOIN\s
                        ai_agent_client ac ON a.id = ac.agent_id
                    WHERE\s
                        a.status = 1
            
            """)
    List<Long> queryValidClientIds();
}