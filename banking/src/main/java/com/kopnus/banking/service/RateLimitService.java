package com.kopnus.banking.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kopnus.banking.config.security.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(100_000)
            .build();

    public ConsumptionProbe tryConsume(String key, RateLimitProperties.Rule rule) {
        Bucket bucket = buckets.get(key, k -> newBucket(rule));
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket newBucket(RateLimitProperties.Rule rule) {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(rule.getCapacity())
                        .refillGreedy(rule.getCapacity(), Duration.ofSeconds(rule.getPeriodSeconds())))
                .build();
    }
}