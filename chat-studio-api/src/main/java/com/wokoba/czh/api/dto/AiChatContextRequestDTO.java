package com.wokoba.czh.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatContextRequestDTO {
    @NotBlank
    private String messageType;
    @NotBlank
    private String message;
    @NotNull
    private LocalDateTime timestamp;

    private String filePatten;
}
