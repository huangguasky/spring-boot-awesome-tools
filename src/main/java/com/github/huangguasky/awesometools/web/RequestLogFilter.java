package com.github.huangguasky.awesometools.web;

import com.github.huangguasky.awesometools.autoconfigure.AwesomeToolsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    private final AwesomeToolsProperties properties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RequestLogFilter(AwesomeToolsProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : properties.getRequestLogExcludePatterns()) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("Request {} {} status={} cost={}ms ip={} ua={} traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    cost,
                    clientIp(request),
                    request.getHeader("User-Agent"),
                    MDC.get("traceId"));
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp;
    }
}
