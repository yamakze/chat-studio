package com.wokoba.czh.domain.agent.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileUrlResource;
import org.springframework.util.MimeTypeUtils;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class AiChatRequestEntity {
    private Long clientId;
    private Long ragId;
    private String userMessage;
    private Integer retryAction ;

    public String getConversationId() {
        return "chat_" + this.clientId;
    }
}
