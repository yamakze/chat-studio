package com.wokoba.czh.infrastructure.adapter.port;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.wokoba.czh.domain.agent.adapter.port.OpenAiService;
import com.wokoba.czh.infrastructure.gateway.dto.ModelListResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OpenAiPort implements OpenAiService {
    // 全局展平索引
    private final Set<String> flatModelIndex = ConcurrentHashMap.newKeySet();

    private final Cache<String, List<String>> modelListCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, List<String>>) notification -> {
                List<String> removedList = notification.getValue();
                if (removedList != null) {
                    removedList.forEach(flatModelIndex::remove);
                }
            })
            .build();

    /**
     * 获取 model list 并更新缓存
     */
    public List<String> modelList(String completionsUrl, String apiKey) {
        String url = completionsUrl.replace("chat/completions", "models");
        List<String> modelList = modelListCache.getIfPresent(url);
        if (modelList != null && !modelList.isEmpty()) return modelList;

        try {
            WebClient webClient = WebClient.create(url);
            ModelListResponseDTO response = webClient.get()
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(ModelListResponseDTO.class)
                    .block();

            modelList = Objects.requireNonNull(response).getData().stream()
                    .map(modelInfo -> {
                        int slashIndex = modelInfo.getId().indexOf('/');
                        return (slashIndex != -1) ?
                                modelInfo.getId().substring(slashIndex + 1) :
                                modelInfo.getId();
                    })
                    .toList();

            // 放入缓存
            modelListCache.put(url, modelList);
            flatModelIndex.addAll(modelList);

            return modelList;
        } catch (Exception e) {
            log.error("获取modelList异常 url:{}", url, e);
            return List.of();
        }
    }

    /**
     * 高效判断某个 model 是否在缓存中
     */
    public boolean containsModel(String model) {
        return flatModelIndex.contains(model);
    }
}

