package com.example.saldo.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

/**
 * Configuração da infraestrutura AWS SQS.
 * Suporta LocalStack para desenvolvimento local via endpoint customizado.
 */
@Configuration
public class SqsConfig {

    @Value("${app.aws.region:us-east-1}")
    private String region;

    @Value("${app.aws.access-key:local}")
    private String accessKey;

    @Value("${app.aws.secret-key:local}")
    private String secretKey;

    @Value("${app.aws.endpoint-override:#{null}}")
    private String endpointOverride;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        var builder = SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            );

        // Permite usar LocalStack em desenvolvimento
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
