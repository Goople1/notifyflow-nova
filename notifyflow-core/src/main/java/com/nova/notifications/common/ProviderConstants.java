package com.nova.notifications.common;

/**
 * Constants for provider configuration, API simulation, and message ID prefixes.
 * <p>
 * Groups all provider-related constants to avoid magic strings in provider classes.
 * Each inner class corresponds to a specific provider implementation.
 * </p>
 */
public final class ProviderConstants {

    private ProviderConstants() {
        // Utility class - prevent instantiation
    }

    // ========== Content Types ==========

    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String CONTENT_TYPE_PLAIN = "text/plain";

    // ========== SendGrid ==========

    public static final class SendGrid {
        public static final String PROVIDER_NAME = "SendGrid";
        public static final String API_PATH = "/v3/mail/send";
        public static final String MESSAGE_ID_PREFIX = "sg-";
        public static final String CREDENTIAL_ERROR = "SendGrid API key must not be null or blank";

        private SendGrid() {}
    }

    // ========== Mailgun ==========

    public static final class Mailgun {
        public static final String PROVIDER_NAME = "Mailgun";
        public static final String API_PATH_TEMPLATE = "/v3/%s/messages";
        public static final String MESSAGE_ID_PREFIX = "mg-";
        public static final String API_KEY_ERROR = "Mailgun API key must not be null or blank";
        public static final String DOMAIN_ERROR = "Mailgun domain must not be null or blank";
        public static final String CONTENT_FIELD_HTML = "html";
        public static final String CONTENT_FIELD_TEXT = "text";

        private Mailgun() {}
    }

    // ========== Twilio ==========

    public static final class Twilio {
        public static final String PROVIDER_NAME = "Twilio";
        public static final String API_PATH = "/2010-04-01/Accounts/%s/Messages.json";
        public static final String MESSAGE_ID_PREFIX = "SM";
        public static final int MESSAGE_SID_LENGTH = 32;
        public static final String ACCOUNT_SID_ERROR = "Twilio Account SID must not be null or blank";
        public static final String AUTH_TOKEN_ERROR = "Twilio Auth Token must not be null or blank";

        private Twilio() {}
    }

    // ========== Vonage ==========

    public static final class Vonage {
        public static final String PROVIDER_NAME = "Vonage";
        public static final String API_PATH = "/sms/json";
        public static final String MESSAGE_ID_PREFIX = "vonage-";
        public static final String API_KEY_ERROR = "Vonage API key must not be null or blank";
        public static final String API_SECRET_ERROR = "Vonage API secret must not be null or blank";

        private Vonage() {}
    }

    // ========== FCM ==========

    public static final class Fcm {
        public static final String PROVIDER_NAME = "FCM";
        public static final String API_PATH = "/v1/projects/-/messages:send";
        public static final String MESSAGE_ID_PREFIX = "projects/-/messages/";
        public static final String SERVER_KEY_ERROR = "FCM server key must not be null or blank";

        private Fcm() {}
    }

    // ========== APNs ==========

    public static final class Apns {
        public static final String PROVIDER_NAME = "APNs";
        public static final String API_PATH_TEMPLATE = "/3/device/%s";
        public static final String MESSAGE_ID_PREFIX = "apns-";
        public static final String TEAM_ID_ERROR = "APNs Team ID must not be null or blank";
        public static final String KEY_ID_ERROR = "APNs Key ID must not be null or blank";
        public static final String BUNDLE_ID_ERROR = "APNs Bundle ID must not be null or blank";

        private Apns() {}
    }

    // ========== Slack ==========

    public static final class Slack {
        public static final String PROVIDER_NAME = "Slack";
        public static final String MESSAGE_ID_PREFIX = "slack-";
        public static final String WEBHOOK_URL_ERROR = "Slack webhook URL must not be null or blank";

        private Slack() {}
    }
}
