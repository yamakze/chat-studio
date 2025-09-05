package com.wokoba.czh.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface ExistModelVersion {
    String message() default "模型版本不存在";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
