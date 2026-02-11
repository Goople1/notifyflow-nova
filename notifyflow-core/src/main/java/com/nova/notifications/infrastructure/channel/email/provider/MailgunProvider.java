package com.nova.notifications.infrastructure.channel.email.provider;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.domain.exception.ProviderException;
import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.ProviderConstants;
import com.nova.notifications.common.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulated Mailgun email provider.
 * <p>
 * Simulates the Mailgun Messages API ({@code POST /v3/{domain}/messages}).
 * Logs the multipart form data structure that would be sent to the real API.
 * </p>
 * <p>
 * Credentials are never logged or exposed in any output.
 * </p>
 */
public class MailgunProvider implements NotificationProvider<EmailNotification> {

    private static final Logger log = LoggerFactory.getLogger(MailgunProvider.class);

    private final String apiKey;
    private final String domain;

    /**
     * Creates a new Mailgun provider.
     *
     * @param apiKey the Mailgun API key (never logged)
     * @param domain the Mailgun sending domain
     */
    public MailgunProvider(String apiKey, String domain) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Mailgun.API_KEY_ERROR);
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Mailgun.DOMAIN_ERROR);
        }
        this.apiKey = apiKey;
        this.domain = domain;
        log.info("Mailgun provider initialized for domain '{}' with API key [***]", domain);
    }

    @Override
    public NotificationResult send(EmailNotification notification) {
        try {
            String contentField = notification.isHtml() ? ProviderConstants.Mailgun.CONTENT_FIELD_HTML : ProviderConstants.Mailgun.CONTENT_FIELD_TEXT;

            log.info("[Mailgun] Simulating POST /v3/{}/messages", domain);
            log.info("[Mailgun] Authorization: Basic api:[***]");
            log.info("[Mailgun] Form data: from={}, to={}, subject={}, {}={}",
                    notification.from(),
                    notification.to(),
                    notification.subject(),
                    contentField,
                    SecurityUtils.truncateForLog(notification.body()));

            if (!notification.cc().isEmpty()) {
                log.info("[Mailgun] Form data: cc={}", String.join(",", notification.cc()));
            }
            if (!notification.bcc().isEmpty()) {
                log.info("[Mailgun] Form data: bcc={}", String.join(",", notification.bcc()));
            }

            String messageId = ProviderConstants.Mailgun.MESSAGE_ID_PREFIX + UUID.randomUUID();
            log.info("[Mailgun] Response: 200 OK, id=<{}.{}>", messageId, domain);

            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("[Mailgun] Failed to send email to {}: {}", notification.to(), e.getMessage());
            throw new ProviderException(ProviderConstants.Mailgun.PROVIDER_NAME, "Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.Mailgun.PROVIDER_NAME;
    }

}
