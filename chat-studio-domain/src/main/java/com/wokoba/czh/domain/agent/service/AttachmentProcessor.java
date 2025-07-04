package com.wokoba.czh.domain.agent.service;

import org.springframework.ai.chat.messages.UserMessage;

public interface AttachmentProcessor {
    UserMessage handleChatAttachments(String userText, String filePatten, Long clientId);
}
