package com.empresa.pedidos.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * CONFIGURACAO — RestTemplate com timeout.
 *
 * Centraliza a configuracao do cliente HTTP para APIs externas.
 * Timeout de conexao e leitura evitam que uma API lenta
 * bloqueie threads do servidor indefinidamente.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}
