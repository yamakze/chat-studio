package com.wokoba.czh.domain.agent.service.advisor;

import com.wokoba.czh.domain.agent.model.valobj.ChatRetryAction;
import com.wokoba.czh.domain.agent.service.memory.CustomChatMemory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.*;

import static com.wokoba.czh.domain.agent.model.valobj.ChatRetryAction.REEDIT_USER_QUESTION;
import static com.wokoba.czh.domain.agent.model.valobj.ChatRetryAction.RETRY_ASSISTANT_RESPONSE;


public class MessageEditAdvisor implements BaseChatMemoryAdvisor {
    public static final String RETRY_ACTION_KEY = "retryActionKey";

    private final String conversationId;
    private final int order;
    private final CustomChatMemory chatMemory;

    public MessageEditAdvisor(String conversationId, CustomChatMemory chatMemory, int order) {
        this.conversationId = conversationId;
        this.chatMemory = chatMemory;
        this.order = order;
    }

    public static MessageEditAdvisor.Builder builder() {
        return new MessageEditAdvisor.Builder();
    }


    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        Map<String, Object> context = request.context();
        ChatRetryAction retryAction = (ChatRetryAction) context.get(RETRY_ACTION_KEY);
        String conversationId = this.getConversationId(context, this.conversationId);

        List<Message> messages = new ArrayList<>(chatMemory.get(conversationId));

        if (Objects.equals(retryAction, RETRY_ASSISTANT_RESPONSE)) {
            return request.mutate()
                    .prompt(request.prompt().augmentUserMessage(userMessage -> userMessage
                            .mutate()
                            .text(String.format("I am not satisfied with the answer. Please reorganize and improve the previous response based on the context and the intent of the question: {%s}", userMessage.getText()))
                            .build()))
                    .build();
        }

        if (Objects.equals(retryAction, REEDIT_USER_QUESTION)) {
            chatMemory.removeLastUserAndAssistantMessages(conversationId);

        }

        messages.addAll(request.prompt().getInstructions());
        ChatClientRequest processedRequest = request.mutate().prompt(request.prompt().mutate().messages(messages).build()).build();
        chatMemory.add(conversationId, processedRequest.prompt().getUserMessage());

        return processedRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        if (chatClientResponse.chatResponse() == null) return chatClientResponse;
        Map<String, Object> context = chatClientResponse.context();
        List<Message> assistantMessages = chatClientResponse.chatResponse().getResults().stream().map(g -> (Message) g.getOutput()).toList();
        String conversationId = this.getConversationId(context, this.conversationId);
        ChatRetryAction retryAction = (ChatRetryAction) context.get(RETRY_ACTION_KEY);

        if (Objects.equals(retryAction, RETRY_ASSISTANT_RESPONSE)) {
            Optional<Message> message = chatMemory.removeLastMessageByType(conversationId, MessageType.ASSISTANT);
            for (Message assistantMessage : assistantMessages) {
                assistantMessage.getMetadata().put("history", message.get());
            }
        }
        this.chatMemory.add(conversationId, assistantMessages);

        return chatClientResponse.mutate().chatResponse(chatClientResponse.chatResponse()).build();
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


    @Override
    public int getOrder() {
        return this.order;
    }

    public static class Builder {

        private CustomChatMemory chatMemory;


        public MessageEditAdvisor build() {
            String conversationId = "default";
            int order = -2147482648;
            return new MessageEditAdvisor(conversationId, this.chatMemory, order);
        }

        public Builder memory(CustomChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }
    }
}
