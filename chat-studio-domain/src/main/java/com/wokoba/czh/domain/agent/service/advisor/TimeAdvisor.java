package com.wokoba.czh.domain.agent.service.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class TimeAdvisor implements BaseAdvisor {

//    @Override
//    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
//        Scheduler scheduler = getScheduler();
//        Mono<ChatClientRequest> requestMono = Mono.just(chatClientRequest).publishOn(scheduler).map(request -> this.before(request, streamAdvisorChain));
//        Objects.requireNonNull(streamAdvisorChain);
//        return requestMono.flatMapMany(streamAdvisorChain::nextStream)
//                .transform(flux -> (new ChatClientMessageAggregator()).aggregateChatClientResponse(flux, (response) -> this.after(response, streamAdvisorChain)));
//    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(userMessage -> {
            Map<String, Object> metadata = userMessage.getMetadata();
            metadata.put("timestamp", LocalDateTime.now());
            return userMessage;
        })).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        AssistantMessage message = Objects.requireNonNull(chatClientResponse.chatResponse()).getResult().getOutput();
        message.getMetadata().put("timestamp", LocalDateTime.now());
        return chatClientResponse.mutate().chatResponse(chatClientResponse.chatResponse()).build();
    }

    @Override
    public int getOrder() {
        return -2147482650;
    }

    @Override
    public String getName() {
        return "timeAdvisor";
    }
}
