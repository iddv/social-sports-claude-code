package com.socialsports.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model:gpt-4}")
    private String model;

    @Value("${openai.api.temperature:0.7}")
    private Double temperature;

    @Value("${openai.api.max-tokens:150}")
    private Integer maxTokens;

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }
} 