package com.nova.notifications.common;

/**
 * Utility class for security-related operations across the library.
 * <p>
 * Provides centralized methods for masking sensitive data (tokens, credentials)
 * and truncating content for safe logging. All providers and services should
 * use these methods instead of implementing their own masking logic.
 * </p>
 */
public final class SecurityUtils {

    /** Placeholder used to mask sensitive values in logs */
    public static final String MASKED_VALUE = "[***]";

    /** Minimum token length to apply partial masking instead of full masking */
    private static final int TOKEN_MASK_THRESHOLD = 8;

    /** Number of characters to show at start and end of a partially masked token */
    private static final int TOKEN_VISIBLE_CHARS = 4;

    /** Ellipsis used between visible parts of a masked token */
    private static final String MASK_ELLIPSIS = "...";

    /** Maximum content length before truncation in log messages */
    private static final int DEFAULT_MAX_LOG_LENGTH = 100;

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Masks a sensitive token for safe logging.
     * <p>
     * Tokens shorter than or equal to {@value TOKEN_MASK_THRESHOLD} characters
     * are fully masked. Longer tokens show the first and last
     * {@value TOKEN_VISIBLE_CHARS} characters with ellipsis in between.
     * </p>
     *
     * @param token the sensitive token to mask
     * @return the masked token string, safe for logging
     */
    public static String maskToken(String token) {
        if (token == null || token.length() <= TOKEN_MASK_THRESHOLD) {
            return MASKED_VALUE;
        }
        return token.substring(0, TOKEN_VISIBLE_CHARS)
                + MASK_ELLIPSIS
                + token.substring(token.length() - TOKEN_VISIBLE_CHARS);
    }

    /**
     * Truncates content for safe logging using the default maximum length.
     *
     * @param content the content to truncate
     * @return truncated content, or empty string if null
     */
    public static String truncateForLog(String content) {
        return truncateForLog(content, DEFAULT_MAX_LOG_LENGTH);
    }

    /**
     * Truncates content for safe logging to the specified maximum length.
     *
     * @param content   the content to truncate
     * @param maxLength maximum number of characters before truncation
     * @return truncated content with ellipsis, or empty string if null
     */
    public static String truncateForLog(String content, int maxLength) {
        if (content == null) return "";
        return content.length() > maxLength
                ? content.substring(0, maxLength) + MASK_ELLIPSIS
                : content;
    }
}
