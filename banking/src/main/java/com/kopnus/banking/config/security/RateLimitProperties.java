package com.kopnus.banking.config.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private Rule auth = new Rule(5, 60);

    private Rule transfer = new Rule(10, 60);

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rule {
        private long capacity;
        private long periodSeconds;
    }
}