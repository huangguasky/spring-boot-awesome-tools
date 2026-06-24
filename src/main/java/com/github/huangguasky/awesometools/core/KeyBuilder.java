package com.github.huangguasky.awesometools.core;

import com.github.huangguasky.awesometools.autoconfigure.AwesomeToolsProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.aspectj.lang.ProceedingJoinPoint;

public class KeyBuilder {

    private final AwesomeToolsProperties properties;

    private final ExpressionResolver expressionResolver;

    public KeyBuilder(AwesomeToolsProperties properties, ExpressionResolver expressionResolver) {
        this.properties = properties;
        this.expressionResolver = expressionResolver;
    }

    public String expressionKey(String namespace, String expression, ProceedingJoinPoint joinPoint) {
        return normalize(namespace, expressionResolver.resolve(expression, joinPoint));
    }

    public String methodKey(String namespace, ProceedingJoinPoint joinPoint) {
        String raw = joinPoint.getSignature().toLongString() + ":" + stableArgs(joinPoint.getArgs());
        return normalize(namespace, sha256(raw));
    }

    public String normalize(String namespace, String key) {
        String cleanKey = key == null || key.isBlank() ? "default" : key.trim();
        return properties.getKeyPrefix() + ":" + namespace + ":" + cleanKey;
    }

    private String stableArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            builder.append(arg == null ? "null" : arg.toString()).append('|');
        }
        return builder.toString();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
