package com.wokoba.czh.config;

import com.wokoba.czh.api.annotation.ExistModelVersion;
import com.wokoba.czh.infrastructure.adapter.validator.ModelVersionValidator;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidatorConfig {

    @Bean
    public Validator validator() {
        return new CustomValidatorFactoryBean();
    }

    private static class CustomValidatorFactoryBean extends LocalValidatorFactoryBean {
        @Override
        protected void postProcessConfiguration(jakarta.validation.Configuration<?> configuration) {
            super.postProcessConfiguration(configuration);
            if (configuration instanceof HibernateValidatorConfiguration hibernateConfig) {
                ConstraintMapping constraintMapping = hibernateConfig.createConstraintMapping();

                constraintMapping
                        .constraintDefinition(ExistModelVersion.class)
                        .validatedBy(ModelVersionValidator.class);

                hibernateConfig.addMapping(constraintMapping);
            }
        }
    }
}
