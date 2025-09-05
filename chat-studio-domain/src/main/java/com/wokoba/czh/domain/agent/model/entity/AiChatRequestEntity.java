package com.wokoba.czh.domain.agent.model.entity;

import com.wokoba.czh.domain.agent.model.valobj.ChatEditAction;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AiChatRequestEntity {
    private Long clientId;
    private Long ragId;
    private String userMessage;
    private Integer editActionCode;

    public String getConversationId() {
        return "chat_" + this.clientId;
    }

    public ChatEditAction getEditAction() {
        return ChatEditAction.fromCode(this.editActionCode);
    }
}

