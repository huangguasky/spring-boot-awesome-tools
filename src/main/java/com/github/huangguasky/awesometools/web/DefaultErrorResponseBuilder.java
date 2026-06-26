package com.github.huangguasky.awesometools.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

public class DefaultErrorResponseBuilder implements ErrorResponseBuilder {

    @Override
    public ApiErrorResponse build(String code, String message, HttpStatus status, HttpServletRequest request,
            Map<String, String> details) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setCode(code);
        response.setMessage(message);
        response.setPath(request == null ? "" : request.getRequestURI());
        response.setTraceId(MDC.get("traceId"));
        response.setDetails(details);
        return response;
    }
}
