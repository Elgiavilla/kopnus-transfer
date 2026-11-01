package com.kopnus.banking.config.documentation;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi authDocumentation(){
        return GroupedOpenApi.builder()
                .group("Auth Management")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi transferDocumentation(){
        return GroupedOpenApi.builder()
                .group("Transfer Management")
                .pathsToMatch("/api/v1/transfers/**")
                .build();
    }

}
