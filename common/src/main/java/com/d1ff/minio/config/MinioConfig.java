package com.d1ff.minio.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "minio", name = "endpoint")
@Slf4j
public class MinioConfig {
    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        log.info("Minio signed endpoint:{}",minioProperties.getEndpoint());
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(),
                        minioProperties.getSecretKey())
                .build();
    }

    @Bean
    @Qualifier("presignedMinioClient")
    public MinioClient presignedMinioClient(){
        String endpoint = minioProperties.getPublicEndpoint() != null
                ? minioProperties.getPublicEndpoint()
                : minioProperties.getEndpoint();

        log.info("Minio public endpoint: {}", endpoint);

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(minioProperties.getAccessKey(),
                        minioProperties.getSecretKey())
                .build();
    }
}
