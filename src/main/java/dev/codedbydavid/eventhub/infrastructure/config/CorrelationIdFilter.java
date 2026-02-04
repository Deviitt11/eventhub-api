package dev.codedbydavid.eventhub.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private static final int MAX_LEN = 64;
    private static final Pattern ALLOWED = Pattern.compile("^[a-zA-Z0-9._-]+$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startNanos = System.nanoTime();

        String correlationId = resolveCorrelationId(request);

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = response.getStatus();

            // One line per request. No body, no cookies, no Authorization.
            log.info("request method={} path={} status={} durationMs={} correlationId={}",
                    method, path, status, durationMs, correlationId);

            MDC.remove(MDC_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String raw = request.getHeader(HEADER_NAME);
        if (raw == null) {
            return UUID.randomUUID().toString();
        }

        String candidate = raw.trim();
        if (candidate.isEmpty() || candidate.length() > MAX_LEN || !ALLOWED.matcher(candidate).matches()) {
            return UUID.randomUUID().toString();
        }

        return candidate;
    }
}