package com.kopnus.banking.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopnus.banking.config.exception.BusinessException;
import com.kopnus.banking.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopnus.banking.shared.constant.ResponseCode;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PREFIX = "/api/v1/auth/";
    private static final String TRANSFER_PATH = "/api/v1/transfers";

    private final RateLimitService rateLimitService;
    private final RateLimitProperties props;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, RateLimitProperties props, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = null;
        RateLimitProperties.Rule rule = null;

        if ("POST".equals(request.getMethod())) {
            String path = request.getRequestURI();
            if (path.startsWith(AUTH_PREFIX)) {
                key = "auth:" + path + ":" + request.getRemoteAddr();
                rule = props.getAuth();
            } else if (path.equals(TRANSFER_PATH)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UUID userId) {
                    key = "transfer:" + userId;
                    rule = props.getTransfer();
                }
            }
        }

        if (key == null) {
            chain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = rateLimitService.tryConsume(key, rule);
        response.setHeader("X-RateLimit-Limit", String.valueOf(rule.getCapacity()));

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        long retryAfter = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new BusinessException(ResponseCode.TOO_MANY_REQUESTS,
                "Terlalu banyak request, coba lagi dalam " + retryAfter + " detik"));
    }
}
