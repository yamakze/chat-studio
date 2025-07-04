package com.wokoba.czh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponseDTO {
    private Integer completionTokens;
    private Integer promptTokens;
    private Integer totalTokens;
    private String outMessage;
}
