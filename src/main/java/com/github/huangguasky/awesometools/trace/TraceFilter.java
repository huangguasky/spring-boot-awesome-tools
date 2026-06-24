package com.github.huangguasky.awesometools.trace;

import com.github.huangguasky.awesometools.autoconfigure.AwesomeToolsProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;

public class TraceFilter implements Filter {

    private final AwesomeToolsProperties properties;

    public TraceFilter(AwesomeToolsProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String previous = MDC.get("traceId");
        String traceId = httpRequest.getHeader(properties.getTraceHeader());
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put("traceId", traceId);
        httpResponse.setHeader(properties.getTraceHeader(), traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            if (previous == null) {
                MDC.remove("traceId");
            } else {
                MDC.put("traceId", previous);
            }
        }
    }
}
