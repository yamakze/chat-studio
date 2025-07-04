package com.wokoba.czh;

import com.wokoba.czh.domain.agent.service.IAiAgentPreheatService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configurable
@EnableScheduling
public class Application implements CommandLineRunner {
    @Resource
    private IAiAgentPreheatService aiAgentPreheatService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) throws Exception {
        aiAgentPreheatService.preheat();
    }
}
