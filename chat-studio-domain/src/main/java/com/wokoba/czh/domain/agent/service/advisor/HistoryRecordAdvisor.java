package com.wokoba.czh.domain.agent.service.advisor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

@Slf4j
public class HistoryRecordAdvisor implements BaseAdvisor {


    @Override
    public String getName() {
        return "testHistoryMessage";
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        log.info("提问:{}", chatClientRequest.prompt().getUserMessage().getText());
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        log.info("回复:{}", chatClientResponse.chatResponse().getResult());

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Scheduler scheduler = getScheduler();
        Mono<ChatClientRequest> requestMono = Mono.just(chatClientRequest).publishOn(scheduler).map(request -> this.before(request, streamAdvisorChain));
        Objects.requireNonNull(streamAdvisorChain);
        return requestMono
                .flatMapMany(streamAdvisorChain::nextStream)
                .transform(flux -> (new ChatClientMessageAggregator()).aggregateChatClientResponse(flux, (response) -> this.after(response, streamAdvisorChain)));
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
