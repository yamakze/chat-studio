package com.wokoba.czh.domain.agent.model.valobj;

import lombok.Data;
import org.springframework.ai.openai.OpenAiChatOptions;

@Data
public class AiClientOptionsVO {
    private Double temperature = 1.0;
    private Integer maxTokens = 3096;
    private Double topP = 1.0;
//    private Double frequencyPenalty = 0.0;
    private Double presencePenalty = 0.0;

    public OpenAiChatOptions buildOpenAiOptions() {
        return OpenAiChatOptions.builder()
                .temperature(this.temperature)
                .maxTokens(this.maxTokens)
//                .frequencyPenalty(this.frequencyPenalty)
                .presencePenalty(this.presencePenalty)
                .topP(this.topP)
                .build();
    }
}
