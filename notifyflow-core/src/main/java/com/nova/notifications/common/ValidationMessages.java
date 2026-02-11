package com.nova.notifications.common;

/**
 * Centralized validation error messages used across all validators.
 * <p>
 * Groups messages by channel to maintain clear ownership while providing
 * a single source of truth. Shared messages (like "is required") use
 * consistent formatting across all channels.
 * </p>
 */
public final class ValidationMessages {

    /** Separator used to join multiple validation errors */
    public static final String ERROR_SEPARATOR = "; ";

    /** Prefix for validation exception messages */
    public static final String VALIDATION_FAILED_PREFIX = "Validation failed: ";

    // ========== Email Validation Messages ==========

    public static final String EMAIL_RECIPIENT_REQUIRED = "Recipient (to) is required";
    public static final String EMAIL_RECIPIENT_INVALID_FORMAT = "Recipient (to) has invalid email format: ";
    public static final String EMAIL_SENDER_REQUIRED = "Sender (from) is required";
    public static final String EMAIL_SENDER_INVALID_FORMAT = "Sender (from) has invalid email format: ";
    public static final String EMAIL_SUBJECT_REQUIRED = "Subject is required";
    public static final String EMAIL_BODY_REQUIRED = "Body is required";
    public static final String EMAIL_CC_INVALID_FORMAT = "CC address has invalid email format: ";
    public static final String EMAIL_BCC_INVALID_FORMAT = "BCC address has invalid email format: ";

    // ========== SMS Validation Messages ==========

    public static final String SMS_PHONE_REQUIRED = "Phone number is required";
    public static final String SMS_PHONE_INVALID_FORMAT = "Phone number must be in E.164 format (e.g., +1234567890): ";
    public static final String SMS_SENDER_REQUIRED = "Sender (from) is required";
    public static final String SMS_MESSAGE_REQUIRED = "Message is required";
    public static final String SMS_MESSAGE_TOO_LONG = "Message exceeds maximum length of %d characters (actual: %d)";

    // ========== Push Validation Messages ==========

    public static final String PUSH_TOKEN_REQUIRED = "Device token is required";
    public static final String PUSH_TOKEN_TOO_SHORT = "Device token must be at least %d characters";
    public static final String PUSH_TITLE_REQUIRED = "Title is required";
    public static final String PUSH_BODY_REQUIRED = "Body is required";

    // ========== Slack Validation Messages ==========

    public static final String SLACK_CHANNEL_REQUIRED = "Channel is required";
    public static final String SLACK_MESSAGE_REQUIRED = "Message is required";

    // ========== Service-level Messages ==========

    public static final String NOTIFICATION_NULL = "Notification must not be null";
    public static final String NO_CHANNEL_CONFIGURED = "No channel configured for type: ";
    public static final String CHANNEL_NOT_AVAILABLE = "Channel %s is not available";
    public static final String UNEXPECTED_ERROR_PREFIX = "Unexpected error: ";
    public static final String ASYNC_ERROR_PREFIX = "Async execution failed: ";
    public static final String TEMPLATE_NOT_FOUND = "Template not found: ";
    public static final String AT_LEAST_ONE_CHANNEL = "At least one notification channel must be configured";

    // ========== Retry Messages ==========

    public static final String MAX_ATTEMPTS_INVALID = "maxAttempts must be at least 1";
    public static final String BACKOFF_MULTIPLIER_INVALID = "backoffMultiplier must be >= 1.0";

    private ValidationMessages() {
        // Utility class - prevent instantiation
    }
}
