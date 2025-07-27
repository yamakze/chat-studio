package com.wokoba.czh.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiChatRequestDTO {
    @NotNull
    private Long chatClientId;
    @NotBlank
    private String message;
    private Long ragId;
    private Integer editActionCode;
}
