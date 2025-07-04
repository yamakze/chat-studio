package com.wokoba.czh.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SPRING_AI_CHAT_MEMORY")
public class SpringAiChatMemory {

    private Long id;

    private String conversationId;

    private String type;

    private String content;

    private LocalDateTime timestamp;

    private String status;
}