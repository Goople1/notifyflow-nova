package com.nova.notifications.domain.exception;

/**
 * Sealed base exception for all notification-related errors.
 * <p>
 * Uses Java 21 sealed classes to define a closed hierarchy of exception types,
 * enabling exhaustive handling in catch blocks and pattern matching.
 * </p>
 */
public sealed class NotificationException extends RuntimeException
        permits ValidationException, ProviderException {

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
