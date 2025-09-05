package com.wokoba.czh.infrastructure.adapter.validator;

import com.wokoba.czh.api.annotation.ExistModelVersion;
import com.wokoba.czh.api.common.ApiConstants;
import com.wokoba.czh.infrastructure.adapter.port.OpenAiPort;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModelVersionValidator implements ConstraintValidator<ExistModelVersion, String> {

    private final OpenAiPort openAiPort;

    @Autowired
    public ModelVersionValidator(OpenAiPort openAiPort) {
        this.openAiPort = openAiPort;
    }

    @Override
    public void initialize(ExistModelVersion constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(
            @Pattern(regexp = "^\\d+_[a-zA-Z0-9._-]+$", message = "模型版本 ID 必须采用「ID-modelVersion」格式")
            String modelVersionId,
            ConstraintValidatorContext constraintValidatorContext) {
        String modelVersion = modelVersionId.split(ApiConstants.CONNECT)[1];
        return openAiPort.containsModel(modelVersion);
    }

}
