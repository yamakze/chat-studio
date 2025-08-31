package com.wokoba.czh.api.annotation;

import com.wokoba.czh.api.validator.ModelVersionValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ModelVersionValidator.class)
public @interface ExistModelVersion {
}
