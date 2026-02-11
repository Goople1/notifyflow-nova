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
 * Simulated SendGrid email provider.
 * <p>
 * Simulates the SendGrid v3 Mail Send API ({@code POST /v3/mail/send}).
 * Logs the JSON structure that would be sent to the real API, including
 * personalizations, sender, subject, and content blocks.
 * </p>
 * <p>
 * Credentials are never logged or exposed in any output.
 * </p>
 */
public class SendGridProvider implements NotificationProvider<EmailNotification> {

    private static final Logger log = LoggerFactory.getLogger(SendGridProvider.class);

    private final String apiKey;

    /**
     * Creates a new SendGrid provider with the given API key.
     *
     * @param apiKey the SendGrid API key (never logged)
     */
    public SendGridProvider(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.SendGrid.CREDENTIAL_ERROR);
        }
        this.apiKey = apiKey;
        log.info("SendGrid provider initialized with API key [***]");
    }

    @Override
    public NotificationResult send(EmailNotification notification) {
        try {
            String contentType = notification.isHtml() ? ProviderConstants.CONTENT_TYPE_HTML : ProviderConstants.CONTENT_TYPE_PLAIN;

            log.info("[SendGrid] Simulating POST /v3/mail/send");
            log.info("[SendGrid] Authorization: Bearer [***]");
            log.info("[SendGrid] Request body: {{" +
                            "\"personalizations\": [{{\"to\": [{{\"email\": \"{}\"}}]" +
                            "{}{}}}], " +
                            "\"from\": {{\"email\": \"{}\"}}, " +
                            "\"subject\": \"{}\", " +
                            "\"content\": [{{\"type\": \"{}\", \"value\": \"{}\"}}]" +
                            "}}",
                    notification.to(),
                    formatCcList(notification),
                    formatBccList(notification),
                    notification.from(),
                    notification.subject(),
                    contentType,
                    SecurityUtils.truncateForLog(notification.body()));

            String messageId = ProviderConstants.SendGrid.MESSAGE_ID_PREFIX + UUID.randomUUID();
            log.info("[SendGrid] Response: 202 Accepted, Message-ID: {}", messageId);

            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("[SendGrid] Failed to send email to {}: {}", notification.to(), e.getMessage());
            throw new ProviderException(ProviderConstants.SendGrid.PROVIDER_NAME, "Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.SendGrid.PROVIDER_NAME;
    }

    private String formatCcList(EmailNotification notification) {
        if (notification.cc().isEmpty()) {
            return "";
        }
        var sb = new StringBuilder(", \"cc\": [");
        for (int i = 0; i < notification.cc().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("{\"email\": \"").append(notification.cc().get(i)).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatBccList(EmailNotification notification) {
        if (notification.bcc().isEmpty()) {
            return "";
        }
        var sb = new StringBuilder(", \"bcc\": [");
        for (int i = 0; i < notification.bcc().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("{\"email\": \"").append(notification.bcc().get(i)).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

}
