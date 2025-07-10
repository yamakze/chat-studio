package com.wokoba.czh.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wokoba.czh.infrastructure.dao.po.SpringAiChatMemory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMemoryDao extends BaseMapper<SpringAiChatMemory> {

    @Insert("<script>" +
            "INSERT INTO SPRING_AI_CHAT_MEMORY (conversation_id, type, content, timestamp, status) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.conversationId}, #{item.type}, #{item.content}, #{item.timestamp}, #{item.status})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<SpringAiChatMemory> springAiChatMemories);
}