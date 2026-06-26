package com.github.huangguasky.awesometools.annotation;

public enum RateLimitType {
    /**
     * Counts requests in a fixed time window and rejects requests after the limit is reached.
     */
    FIXED_WINDOW,

    /**
     * Counts requests in the latest rolling time window and rejects requests after the limit is reached.
     */
    SLIDING_WINDOW,

    /**
     * Buffers requests in a bucket and leaks them at a steady rate.
     */
    LEAKY_BUCKET,

    /**
     * Refills tokens at a steady rate and consumes one token for each accepted request.
     */
    TOKEN_BUCKET
}
