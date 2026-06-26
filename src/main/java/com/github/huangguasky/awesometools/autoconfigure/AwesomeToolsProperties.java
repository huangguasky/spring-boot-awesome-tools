package com.github.huangguasky.awesometools.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "awesome-tools")
public class AwesomeToolsProperties {

    private boolean enabled = true;

    private String keyPrefix = "awesome-tools";

    private String traceHeader = "X-Trace-Id";

    private boolean traceFilterEnabled = true;

    private boolean exceptionHandlerEnabled = true;

    private boolean requestLogEnabled = true;

    private String[] requestLogExcludePatterns = {"/actuator/**", "/swagger/**", "/v3/api-docs/**"};

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getTraceHeader() {
        return traceHeader;
    }

    public void setTraceHeader(String traceHeader) {
        this.traceHeader = traceHeader;
    }

    public boolean isTraceFilterEnabled() {
        return traceFilterEnabled;
    }

    public void setTraceFilterEnabled(boolean traceFilterEnabled) {
        this.traceFilterEnabled = traceFilterEnabled;
    }

    public boolean isExceptionHandlerEnabled() {
        return exceptionHandlerEnabled;
    }

    public void setExceptionHandlerEnabled(boolean exceptionHandlerEnabled) {
        this.exceptionHandlerEnabled = exceptionHandlerEnabled;
    }

    public boolean isRequestLogEnabled() {
        return requestLogEnabled;
    }

    public void setRequestLogEnabled(boolean requestLogEnabled) {
        this.requestLogEnabled = requestLogEnabled;
    }

    public String[] getRequestLogExcludePatterns() {
        return requestLogExcludePatterns;
    }

    public void setRequestLogExcludePatterns(String[] requestLogExcludePatterns) {
        this.requestLogExcludePatterns = requestLogExcludePatterns;
    }
}
