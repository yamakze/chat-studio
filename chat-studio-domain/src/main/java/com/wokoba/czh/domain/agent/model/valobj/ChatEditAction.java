package com.wokoba.czh.domain.agent.model.valobj;

import com.wokoba.czh.domain.agent.service.memory.CustomChatMemory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum ChatEditAction {
    // 默认无操作
    NONE(0, "none", true, true) {
        @Override
        public ChatClientRequest executeBefore(ChatClientRequest request, CustomChatMemory chatMemory, String conversationId) {
            return request;
        }

        @Override
        public List<Message> executeAfter(List<Message> currentMessages, CustomChatMemory chatMemory, String conversationId) {
            return currentMessages;
        }
    },

    // 移除上一次AI回复并重新回答
    RETRY_ASSISTANT_RESPONSE(1, "retryAssistant", false, true) {
        @Override
        public ChatClientRequest executeBefore(ChatClientRequest request, CustomChatMemory chatMemory, String conversationId) {
            return request.mutate()
                    .prompt(request.prompt().augmentUserMessage(userMessage -> userMessage
                            .mutate()
                            .text(String.format("I am not satisfied with the answer. Please reorganize and improve the previous response based on the context and the intent of the question: {%s}", userMessage.getText()))
                            .build()))
                    .build();
        }

        @Override
        public List<Message> executeAfter(List<Message> currentMessages, CustomChatMemory chatMemory, String conversationId) {
            Optional<Message> message = chatMemory.removeLastMessageByType(conversationId, MessageType.ASSISTANT);
            for (Message assistantMessage : currentMessages) {
                assistantMessage.getMetadata().put("history", message.get());
            }
            return currentMessages;
        }
    },

    // 移除上一次用户问题与AI回复，重新提问
    REEDIT_USER_QUESTION(2, "reeditQuestion", true, true) {
        @Override
        public ChatClientRequest executeBefore(ChatClientRequest request, CustomChatMemory chatMemory, String conversationId) {
            chatMemory.removeLastUserAndAssistantMessages(conversationId);
            return request;
        }

        @Override
        public List<Message> executeAfter(List<Message> currentMessages, CustomChatMemory chatMemory, String conversationId) {
            return currentMessages;
        }
    },

    // 继续上次未完成的回答
    PROCEED(3, "proceed", false, true) {
        @Override
        public ChatClientRequest executeBefore(ChatClientRequest request, CustomChatMemory chatMemory, String conversationId) {
            return request;
        }

        @Override
        public List<Message> executeAfter(List<Message> currentMessages, CustomChatMemory chatMemory, String conversationId) {
            Optional<Message> removedMessage = chatMemory.removeLastMessageByType(conversationId, MessageType.ASSISTANT);
            return currentMessages.stream()
                    .map(message -> (AssistantMessage) message)
                    .map(assistantMessage -> new AssistantMessage(assistantMessage.getText() + removedMessage.get().getText(), assistantMessage.getMetadata(), assistantMessage.getToolCalls(), assistantMessage.getMedia()))
                    .map(assistantMessage -> (Message) assistantMessage)
                    .toList();
        }
    }

    ;

    private final Integer code;
    private final String label;
    private final Boolean UserPersistenceFlag;
    private final Boolean aSSISTANTPersistenceFlag;

    public static ChatEditAction fromCode(Integer code) {
        if (code == null) return NONE;
        for (ChatEditAction action : values()) {
            if (action.code.equals(code)) return action;
        }
        return NONE;
    }

    /**
     * 预处理聊天记忆。
     *
     * @param request        对话请求体
     * @param chatMemory     聊天记忆体
     * @param conversationId 当前会话ID
     */
    public abstract ChatClientRequest executeBefore(ChatClientRequest request, CustomChatMemory chatMemory, String conversationId);

    /**
     * 处理模型新生成的消息。
     *
     * @param currentMessages 模型响应
     * @param chatMemory      聊天记忆体
     * @param conversationId  当前会话ID
     * @return 已处理的消息集合
     */
    public abstract List<Message> executeAfter(List<Message> currentMessages, CustomChatMemory chatMemory, String conversationId);

}
