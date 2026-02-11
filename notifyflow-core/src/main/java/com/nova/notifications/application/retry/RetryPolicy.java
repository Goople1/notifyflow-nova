package com.nova.notifications.application.retry;

import com.nova.notifications.common.ValidationMessages;

import java.time.Duration;

/**
 * Defines the retry policy for failed notification sends.
 * <p>
 * Configurable maximum attempts, initial delay, and backoff multiplier.
 * Uses exponential backoff by default to avoid overwhelming providers.
 * </p>
 *
 * @param maxAttempts       maximum number of send attempts (including first try)
 * @param initialDelay      delay before first retry
 * @param backoffMultiplier multiplier applied to delay after each retry (e.g., 2.0 for exponential)
 * @param maxDelay          maximum delay cap to prevent excessive waits
 */
public record RetryPolicy(
        int maxAttempts,
        Duration initialDelay,
        double backoffMultiplier,
        Duration maxDelay
) {

    public RetryPolicy {
        if (maxAttempts < 1) throw new IllegalArgumentException(ValidationMessages.MAX_ATTEMPTS_INVALID);
        if (backoffMultiplier < 1.0) throw new IllegalArgumentException(ValidationMessages.BACKOFF_MULTIPLIER_INVALID);
    }

    /**
     * Default policy: 3 attempts, 1s initial, 2x backoff, 30s max.
     */
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(30));
    }

    /**
     * No retry - send once only.
     */
    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, Duration.ZERO, 1.0, Duration.ZERO);
    }

    /**
     * Calculates the delay for a given attempt number (0-indexed).
     */
    public Duration delayForAttempt(int attempt) {
        if (attempt <= 0) return Duration.ZERO;
        long delayMs = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt - 1));
        return Duration.ofMillis(Math.min(delayMs, maxDelay.toMillis()));
    }
}
