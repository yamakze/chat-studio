package com.wokoba.czh.infrastructure.adapter.port;

import com.wokoba.czh.domain.agent.adapter.port.OpenAiService;
import com.wokoba.czh.infrastructure.gateway.dto.ModelListResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class OpenAiPort implements OpenAiService {

    public List<String> modelList(String baseUrl, String completionsPath, String apiKey) {
        String url = baseUrl + completionsPath.replace("chat/completions", "models");
        try {
            WebClient webClient = WebClient.create(url);
            ModelListResponseDTO response = webClient.get()
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(ModelListResponseDTO.class)
                    .block();

            return Objects.requireNonNull(response).getData().stream()
                    .map(modelInfo -> {
                        int slashIndex = modelInfo.getId().indexOf('/');
                        if (slashIndex != -1) {
                            return modelInfo.getId().substring(slashIndex + 1);
                        } else {
                            return modelInfo.getId();
                        }
                    })
                    .toList();
        } catch (Exception e) {
            log.error("获取modelList异常 url:{}", url, e);
            return List.of();
        }
    }



}
