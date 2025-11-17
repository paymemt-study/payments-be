package com.paymentsbe.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient tossRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.tosspayments.com/v1")
                .build();
    }
}
