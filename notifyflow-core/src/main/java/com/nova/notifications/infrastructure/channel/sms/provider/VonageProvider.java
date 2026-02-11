package com.nova.notifications.infrastructure.channel.sms.provider;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.domain.exception.ProviderException;
import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.ProviderConstants;
import com.nova.notifications.common.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulated Vonage (formerly Nexmo) SMS provider.
 * <p>
 * Simulates the Vonage SMS API ({@code POST /sms/json}).
 * Logs the JSON request structure that would be sent to the real API.
 * </p>
 * <p>
 * Credentials (apiKey, apiSecret) are never logged or exposed.
 * </p>
 */
public class VonageProvider implements NotificationProvider<SmsNotification> {

    private static final Logger log = LoggerFactory.getLogger(VonageProvider.class);

    private final String apiKey;
    private final String apiSecret;

    /**
     * Creates a new Vonage provider.
     *
     * @param apiKey    the Vonage API key (never logged)
     * @param apiSecret the Vonage API secret (never logged)
     */
    public VonageProvider(String apiKey, String apiSecret) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Vonage.API_KEY_ERROR);
        }
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Vonage.API_SECRET_ERROR);
        }
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        log.info("Vonage provider initialized with API key [***] and API secret [***]");
    }

    @Override
    public NotificationResult send(SmsNotification notification) {
        try {
            log.info("[Vonage] Simulating POST /sms/json");
            log.info("[Vonage] Request body: {{" +
                            "\"api_key\": \"[***]\", " +
                            "\"api_secret\": \"[***]\", " +
                            "\"from\": \"{}\", " +
                            "\"to\": \"{}\", " +
                            "\"text\": \"{}\"" +
                            "}}",
                    notification.from(),
                    notification.phoneNumber(),
                    SecurityUtils.truncateForLog(notification.message()));

            String messageId = ProviderConstants.Vonage.MESSAGE_ID_PREFIX + UUID.randomUUID();
            log.info("[Vonage] Response: 200 OK, message-id: {}", messageId);

            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("[Vonage] Failed to send SMS to {}: {}", notification.phoneNumber(), e.getMessage());
            throw new ProviderException(ProviderConstants.Vonage.PROVIDER_NAME, "Failed to send SMS: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.Vonage.PROVIDER_NAME;
    }

}
