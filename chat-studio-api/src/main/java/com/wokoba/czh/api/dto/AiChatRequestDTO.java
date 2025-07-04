package com.wokoba.czh.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AiChatRequestDTO {
    @NotNull
    private Long chatClientId;
    @NotBlank
    private String message;
    private Long ragId;
    private Integer retryActionCode;
}
