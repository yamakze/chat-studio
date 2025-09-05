package com.wokoba.czh.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiClientVO {
    private Long id;

    private String clientName;

    private String description;

    private Integer status;
}
