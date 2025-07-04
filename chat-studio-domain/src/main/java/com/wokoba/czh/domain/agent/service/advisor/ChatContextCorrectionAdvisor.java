package com.wokoba.czh.domain.agent.service.advisor;

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.wokoba.czh.domain.agent.model.valobj.ChatRetryActionType.REEDIT_LAST_USER_QUESTION;
import static com.wokoba.czh.domain.agent.model.valobj.ChatRetryActionType.RETRY_LAST_ASSISTANT_RESPONSE;


public class ChatContextCorrectionAdvisor implements BaseChatMemoryAdvisor {
    public static final String RETRY_ACTION_KEY = "retryActionKey";

    private final String conversationId;
    private final int order;
    private final ChatMemory chatMemory;

    public ChatContextCorrectionAdvisor(String conversationId, ChatMemory chatMemory, int order) {
        this.conversationId = conversationId;
        this.chatMemory = chatMemory;
        this.order = order;
    }

    public static ChatContextCorrectionAdvisor.Builder builder() {
        return new ChatContextCorrectionAdvisor.Builder();
    }


    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        Map<String, Object> context = request.context();
        Integer actionCode = (Integer) context.get(RETRY_ACTION_KEY);
        String conversationId = this.getConversationId(context, this.conversationId);

        List<Message> messages = new ArrayList<>(chatMemory.get(conversationId));

        if (Objects.equals(actionCode, RETRY_LAST_ASSISTANT_RESPONSE.getCode())) {
            request = request.mutate().prompt(
                            request.prompt()
                                    .augmentUserMessage(userMessage -> userMessage
                                            .mutate()
                                            .text(String.format("I am not satisfied with the answer. Please reorganize and improve the previous response based on the context and the intent of the question: {%s}", userMessage.getText()))
                                            .build())
                    )
                    .build();
        }

        if (Objects.equals(actionCode, REEDIT_LAST_USER_QUESTION.getCode())) {
            clearLastUserQA(conversationId,messages);
        }

        messages.addAll(request.prompt().getInstructions());
        ChatClientRequest processedRequest = request.mutate().prompt(request.prompt().mutate().messages(messages).build()).build();
        chatMemory.add(conversationId, processedRequest.prompt().getUserMessage());

        return processedRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        if (chatClientResponse.chatResponse() != null) {
            List<Message> assistantMessages = chatClientResponse.chatResponse().getResults().stream().map(g -> (Message) g.getOutput()).toList();
            this.chatMemory.add(this.getConversationId(chatClientResponse.context(), this.conversationId), assistantMessages);
        }
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Scheduler scheduler = this.getScheduler();
        Mono<ChatClientRequest> var10000 = Mono.just(chatClientRequest).publishOn(scheduler).map((request) -> this.before(request, streamAdvisorChain));
        Objects.requireNonNull(streamAdvisorChain);
        return var10000.flatMapMany(streamAdvisorChain::nextStream)
                .transform(flux -> new ChatClientMessageAggregator()
                        .aggregateChatClientResponse(flux, response -> this.after(response, streamAdvisorChain)));
    }


    /**
     * 清除上次问答
     */
    private void clearLastUserQA(String conversationId, List<Message> history) {
        removeLastMessageOfType(history, MessageType.ASSISTANT);
        removeLastMessageOfType(history, MessageType.USER);

        this.chatMemory.clear(conversationId);
        this.chatMemory.add(conversationId, history);
    }

    private void removeLastMessageOfType(List<Message> messages, MessageType role) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getMessageType() == role) {
                messages.remove(i);
                break;
            }
        }
    }


    @Override
    public int getOrder() {
        return this.order;
    }


    public static class Builder {

        private ChatMemory chatMemory;


        public ChatContextCorrectionAdvisor build() {
            String conversationId = "default";
            int order = -2147482648;
            return new ChatContextCorrectionAdvisor(conversationId, this.chatMemory, order);
        }

        public Builder memory(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }
    }
}
