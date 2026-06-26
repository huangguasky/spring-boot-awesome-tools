package com.github.huangguasky.awesometools.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;

public interface ErrorResponseBuilder {

    ApiErrorResponse build(String code, String message, HttpStatus status, HttpServletRequest request,
            Map<String, String> details);
}
