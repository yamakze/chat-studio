package com.wokoba.czh.trigger.http;

import com.wokoba.czh.api.IAiChatApi;
import com.wokoba.czh.api.dto.AiChatContextRequestDTO;
import com.wokoba.czh.api.dto.AiChatRequestDTO;
import com.wokoba.czh.api.dto.ChatResponseDTO;
import com.wokoba.czh.domain.agent.model.entity.AiChatRequestEntity;
import com.wokoba.czh.domain.agent.service.AttachmentProcessor;
import com.wokoba.czh.domain.agent.service.IAiAgentService;
import com.wokoba.czh.domain.agent.service.chat.AiChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class AiChatController implements IAiChatApi {
    @Autowired
    private AiChatService aiChatService;
    @Autowired
    private AttachmentProcessor attachmentProcessor;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private IAiAgentService agentService;

    /**
     * 单轮对话
     */
    @Override
    @GetMapping
    public ResponseEntity<ChatResponseDTO> aiChat(@Valid AiChatRequestDTO requestDTO) {
        ChatResponse chatResponse = aiChatService.aiChat(new AiChatRequestEntity()
                .setUserMessage(requestDTO.getMessage())
                .setRagId(requestDTO.getRagId())
                .setRetryAction(requestDTO.getRetryActionCode())
                .setClientId(requestDTO.getChatClientId()));
        Integer completionTokens = chatResponse.getMetadata().getUsage().getCompletionTokens();
        Integer promptTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
        Integer totalTokens = chatResponse.getMetadata().getUsage().getTotalTokens();
        String outPut = chatResponse.getResult().getOutput().getText();
        return ResponseEntity.ok(new ChatResponseDTO(completionTokens, promptTokens, totalTokens, outPut));
    }

    /**
     * 流式对话
     */
    @Override
    @GetMapping("/stream")
    public ResponseEntity<Flux<ChatResponseDTO>> aiChatStream(@Valid AiChatRequestDTO requestDTO) {
        Flux<ChatResponse> responseFlux = aiChatService.aiChatStream(new AiChatRequestEntity()
                .setUserMessage(requestDTO.getMessage())
                .setRagId(requestDTO.getRagId())
                .setRetryAction(requestDTO.getRetryActionCode())
                .setClientId(requestDTO.getChatClientId()));

        return ResponseEntity.ok(responseFlux.map(response -> {
            Usage usage = response.getMetadata().getUsage();
            Integer completionTokens = usage.getCompletionTokens();
            Integer promptTokens = usage.getPromptTokens();
            Integer totalTokens = usage.getTotalTokens();
            String outPut = response.getResult().getOutput().getText();
            return new ChatResponseDTO(completionTokens, promptTokens, totalTokens, outPut);
        }));
    }

    @Override
    @GetMapping("/agent")
    public ResponseEntity<String> agentChat(@NotNull Long clientId, @NotBlank String input) {
//        return ResponseEntity.ok(agentService.agentChat(clientId,input));
        return ResponseEntity.ok("agent功能待完善");
    }

    /**
     * 装配对话上下文
     */
    @Override
    @PutMapping("/{clientId}")
    public ResponseEntity<Boolean> armoryChatContext(@PathVariable Long clientId, @RequestBody List<AiChatContextRequestDTO> requestDTO) {
        try {
            String conversationId = "chat_" + clientId;
            if (!chatMemory.get(conversationId).isEmpty()) return ResponseEntity.ok(true);

            List<Message> historyMessages = requestDTO.stream()
                    .map(context -> convertToMessage(context, clientId))
                    .filter(Objects::nonNull)
                    .toList();

            chatMemory.add(conversationId, historyMessages);

            log.info("装配对话上下文成功 clientId:{}, 消息数量:{}", clientId, historyMessages.size());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("装配对话上下文失败 clientId:{}", clientId, e);
            return ResponseEntity.internalServerError().body(false);
        }
    }

    /**
     * 清空对话上下文
     */
    @Override
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Boolean> clearChatContext(@PathVariable Long clientId) {
        try {
            String conversationId = "chat_" + clientId;
            chatMemory.clear(conversationId);
            log.info("清空对话上下文成功 clientId:{}", clientId);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("清空上下文失败 clientId:{}", clientId, e);
            return ResponseEntity.internalServerError().body(false);
        }
    }

    private Message convertToMessage(AiChatContextRequestDTO context, Long clientId) {
        return switch (context.getMessageType().toLowerCase()) {
            case "user" -> {
//                UserMessage.builder()
//                        .text(context.getMessage())
//                        .metadata(Map.of("timestamp", context.getTimestamp()))
//                        .build();
                UserMessage userMessage = attachmentProcessor.handleChatAttachments(
                        context.getMessage(),
                        Optional.ofNullable(context.getFilePatten()).orElse("@file:([^\\s]+)"),
                        clientId);
                userMessage.getMetadata().put("timestamp", context.getTimestamp());
                yield userMessage;
            }

            case "assistant" -> new AssistantMessage(context.getMessage(), Map.of("timestamp", context.getTimestamp()));
            default -> {
                log.warn("未知的消息类型:{}", context.getMessageType());
                yield null;
            }
        };
    }


}