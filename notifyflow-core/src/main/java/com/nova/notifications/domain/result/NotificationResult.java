package com.nova.notifications.domain.result;

import com.nova.notifications.common.ErrorSource;

import java.time.Instant;
import java.util.Optional;

/**
 * Immutable result of a notification send attempt.
 * <p>
 * Uses the Result pattern instead of exceptions for control flow,
 * enabling safe batch processing where one failure doesn't abort others.
 * Provides detailed error information including source, message, and cause.
 * </p>
 *
 * @param successful      whether the notification was sent successfully
 * @param notificationId unique identifier for tracking (provider-generated or internal)
 * @param errorSource  category of the error (VALIDATION, PROVIDER, CONFIGURATION, etc.)
 * @param errorMessage human-readable description of what went wrong
 * @param cause        original exception if available
 * @param timestamp    when the result was created
 */
public record NotificationResult(
        boolean successful,
        String notificationId,
        String errorSource,
        String errorMessage,
        Throwable cause,
        Instant timestamp
) {

    /**
     * Creates a successful result with a notification ID.
     */
    public static NotificationResult success(String notificationId) {
        return new NotificationResult(true, notificationId, null, null, null, Instant.now());
    }

    /**
     * Creates a successful result without a notification ID.
     */
    public static NotificationResult success() {
        return new NotificationResult(true, null, null, null, null, Instant.now());
    }

    /**
     * Creates a failure result from a validation error.
     */
    public static NotificationResult validationError(String message) {
        return new NotificationResult(false, null, ErrorSource.VALIDATION, message, null, Instant.now());
    }

    /**
     * Creates a failure result from a provider/sending error.
     */
    public static NotificationResult providerError(String providerName, String message, Throwable cause) {
        return new NotificationResult(false, null, ErrorSource.provider(providerName), message, cause, Instant.now());
    }

    /**
     * Creates a failure result from a configuration error.
     */
    public static NotificationResult configurationError(String message) {
        return new NotificationResult(false, null, ErrorSource.CONFIGURATION, message, null, Instant.now());
    }

    /**
     * Creates a failure result from an unexpected system error.
     */
    public static NotificationResult systemError(String message, Throwable cause) {
        return new NotificationResult(false, null, ErrorSource.SYSTEM, message, cause, Instant.now());
    }

    public Optional<String> getNotificationId() {
        return Optional.ofNullable(notificationId);
    }

    public Optional<String> getErrorSource() {
        return Optional.ofNullable(errorSource);
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<Throwable> getCause() {
        return Optional.ofNullable(cause);
    }
}
