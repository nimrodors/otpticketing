package com.example.otp.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi ticketApi() {
        return GroupedOpenApi.builder()
                .group("ticket-api")
                .packagesToScan("com.example.otp.ticket.controller")
                .pathsToMatch("/ticket/**")
                .build();
    }

    @Bean
    public GroupedOpenApi apiApi() {
        return GroupedOpenApi.builder()
                .group("api-api")
                .packagesToScan("com.example.otp.api.controller")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi coreApi() {
        return GroupedOpenApi.builder()
                .group("core-api")
                .packagesToScan("com.example.otp.core")
                .pathsToMatch("/core/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("OTP API")
                        .description("Ticketing sSystem API")
                        .version("v1.0.0"));
    }

}
