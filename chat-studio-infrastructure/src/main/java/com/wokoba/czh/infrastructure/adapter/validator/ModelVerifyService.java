package com.wokoba.czh.infrastructure.adapter.validator;

import com.wokoba.czh.api.annotation.ExistModelVersion;
import com.wokoba.czh.api.validator.ModelVersionValidator;
import com.wokoba.czh.infrastructure.adapter.port.OpenAiPort;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ModelVerifyService implements ModelVersionValidator {
    private final OpenAiPort openAiPort;

    public ModelVerifyService(OpenAiPort openAiPort) {
        this.openAiPort = openAiPort;
    }

    @Override
    public void initialize(ExistModelVersion constraintAnnotation) {
        ModelVersionValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String modelVersion, ConstraintValidatorContext constraintValidatorContext) {
        return openAiPort.containsModel(modelVersion);
    }
}
