package com.wokoba.czh.api.validator;

import com.wokoba.czh.api.annotation.ExistModelVersion;
import jakarta.validation.ConstraintValidator;

public interface ModelVersionValidator extends ConstraintValidator<ExistModelVersion,String> {
}
