package com.wokoba.czh.domain.agent.service.advisor;

import com.wokoba.czh.domain.agent.service.AttachmentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;

import java.util.Map;

@Slf4j
public class CustomMediaAdvisor implements BaseAdvisor {
    public static final String PATTERN_KEY = "patternKey";
    public static final String CLIENT_ID_KEY = "clientIdKey";
    private static final String DEFAULT_FILE_PATTERN = "@file:([^\\s]+)";

    private String filePattern;
    private final AttachmentProcessor attachmentProcessor;

    public CustomMediaAdvisor(AttachmentProcessor attachmentProcessor, String filePattern) {
        this.filePattern = filePattern;
        this.attachmentProcessor = attachmentProcessor;
    }

    public CustomMediaAdvisor(AttachmentProcessor attachmentProcessor) {
        this(attachmentProcessor, DEFAULT_FILE_PATTERN);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        Map<String, Object> context = request.context();
        if (!isValidRequest(context)) {
            return request;
        }
        filePattern = (String) context.getOrDefault(PATTERN_KEY, DEFAULT_FILE_PATTERN);
        Long clientId = (Long) context.get(CLIENT_ID_KEY);
        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(message -> attachmentProcessor.handleChatAttachments(message.getText(), filePattern, clientId)))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }


    private boolean isValidRequest(Map<String, Object> adviseContext) {
        return adviseContext.containsKey(CLIENT_ID_KEY);
    }


    @Override
    public String getName() {
        return "customMediaAdvisor";
    }


    @Override
    public int getOrder() {
        return -2147482649;
    }

}