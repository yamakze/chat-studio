CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
                                                     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
                                                     conversation_id VARCHAR(64) NOT NULL COMMENT '对话 ID',
                                                     type VARCHAR(20) NOT NULL COMMENT '消息类型（USER / ASSISTANT / SYSTEM）',
                                                     content TEXT NOT NULL COMMENT '消息内容',
                                                     timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间戳',

                                                     INDEX idx_conversation_id (conversation_id),
                                                     INDEX idx_conversation_time (conversation_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Spring AI 聊天记忆表';
