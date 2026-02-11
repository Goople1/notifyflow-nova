package com.nova.notifications.domain.exception;

/**
 * Exception thrown when a provider fails to send a notification.
 * <p>
 * Wraps the underlying provider error with the provider name
 * for clear identification of which integration failed.
 * </p>
 */
public final class ProviderException extends NotificationException {

    private final String providerName;

    public ProviderException(String providerName, String message) {
        super("[" + providerName + "] " + message);
        this.providerName = providerName;
    }

    public ProviderException(String providerName, String message, Throwable cause) {
        super("[" + providerName + "] " + message, cause);
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}
