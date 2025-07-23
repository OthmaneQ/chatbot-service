package com.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GoogleAIConfig {

    @Value("${google.ai.api.key}")
    private String googleAiApiKey;

    @Value("${google.ai.api.url:https://generativelanguage.googleapis.com}")
    private String googleAiApiUrl;

    @Bean
    public WebClient googleAiWebClient() {
        return WebClient.builder()
                .baseUrl(googleAiApiUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public String googleAiApiKey() {
        return googleAiApiKey;
    }
}