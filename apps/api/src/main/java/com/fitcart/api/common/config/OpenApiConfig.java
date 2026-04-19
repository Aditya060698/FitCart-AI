package com.fitcart.api.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fitCartOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FitCart AI API")
                        .description("Core backend API for FitCart AI")
                        .version("v1")
                        .contact(new Contact().name("FitCart AI"))
                        .license(new License().name("Internal project starter")));
    }
}
