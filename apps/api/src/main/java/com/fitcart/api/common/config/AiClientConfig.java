package com.fitcart.api.common.config;

import com.fitcart.api.common.exception.UpstreamServiceException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiClientConfig {

    @Bean
    public RestClient aiRestClient(AiServiceProperties properties, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            throw new UpstreamServiceException(
                                    "AI service returned " + response.getStatusCode().value()
                            );
                        }
                )
                .build();
    }
}
