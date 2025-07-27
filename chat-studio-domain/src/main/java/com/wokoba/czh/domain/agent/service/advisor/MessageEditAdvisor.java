package com.wokoba.czh.domain.agent.service.advisor;

import com.wokoba.czh.domain.agent.model.valobj.ChatEditAction;
import com.wokoba.czh.domain.agent.service.memory.CustomChatMemory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MessageEditAdvisor implements BaseChatMemoryAdvisor {
    public static final String RETRY_ACTION_KEY = "editActionKey";

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
        ChatEditAction editAction = (ChatEditAction) context.get(RETRY_ACTION_KEY);
        String conversationId = this.getConversationId(context, this.conversationId);

        ChatClientRequest processedRequest = editAction.executeBefore(request, chatMemory, conversationId);

        List<Message> messages = new ArrayList<>(chatMemory.get(conversationId));
        messages.addAll(processedRequest.prompt().getInstructions());
        if (editAction.getUserPersistenceFlag())
            chatMemory.add(conversationId, processedRequest.prompt().getUserMessage());
        return request.mutate().prompt(request.prompt().mutate().messages(messages).build()).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        if (chatClientResponse.chatResponse() == null) return chatClientResponse;

        Map<String, Object> context = chatClientResponse.context();
        List<Message> assistantMessages = chatClientResponse.chatResponse().getResults().stream().map(g -> (Message) g.getOutput()).toList();
        String conversationId = this.getConversationId(context, this.conversationId);
        ChatEditAction editAction = (ChatEditAction) context.get(RETRY_ACTION_KEY);

        List<Message> processedMessages = editAction.executeAfter(assistantMessages, chatMemory, conversationId);
        if (editAction.getASSISTANTPersistenceFlag())
            chatMemory.add(conversationId, processedMessages);
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
