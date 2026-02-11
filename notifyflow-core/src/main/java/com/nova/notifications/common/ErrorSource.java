package com.nova.notifications.common;

/**
 * Constants for notification error source categories.
 * <p>
 * Defines the error source identifiers used in {@link com.nova.notifications.domain.result.NotificationResult}
 * to categorize failures. These constants ensure consistency between error creation
 * and error checking across the library (e.g., retry logic skips VALIDATION errors).
 * </p>
 */
public final class ErrorSource {

    /** Validation errors (invalid input, missing fields) - should NOT be retried */
    public static final String VALIDATION = "VALIDATION";

    /** Provider communication errors - prefix followed by provider name */
    public static final String PROVIDER_PREFIX = "PROVIDER:";

    /** Configuration errors (missing channel, unavailable) */
    public static final String CONFIGURATION = "CONFIGURATION";

    /** Unexpected system errors (runtime exceptions, thread issues) */
    public static final String SYSTEM = "SYSTEM";

    private ErrorSource() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a provider error source identifier with the provider name.
     *
     * @param providerName the name of the provider
     * @return formatted error source string
     */
    public static String provider(String providerName) {
        return PROVIDER_PREFIX + providerName;
    }
}
