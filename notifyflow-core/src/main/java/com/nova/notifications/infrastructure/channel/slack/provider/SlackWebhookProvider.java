package com.nova.notifications.infrastructure.channel.slack.provider;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.domain.exception.ProviderException;
import com.nova.notifications.domain.model.SlackNotification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.ProviderConstants;
import com.nova.notifications.common.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulated Slack Incoming Webhook provider.
 * <p>
 * Simulates the Slack Incoming Webhooks API ({@code POST webhookUrl}).
 * Logs the JSON payload structure that would be sent, including text,
 * channel, username, and icon_emoji fields.
 * </p>
 * <p>
 * The webhook URL is treated as a credential and is never logged in full.
 * </p>
 */
public class SlackWebhookProvider implements NotificationProvider<SlackNotification> {

    private static final Logger log = LoggerFactory.getLogger(SlackWebhookProvider.class);

    private final String webhookUrl;

    /**
     * Creates a new Slack webhook provider.
     *
     * @param webhookUrl the Slack Incoming Webhook URL (never logged in full)
     */
    public SlackWebhookProvider(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Slack.WEBHOOK_URL_ERROR);
        }
        this.webhookUrl = webhookUrl;
        log.info("Slack webhook provider initialized with webhook URL [***]");
    }

    @Override
    public NotificationResult send(SlackNotification notification) {
        try {
            log.info("[Slack] Simulating POST to webhook URL [***]");
            log.info("[Slack] Payload: {{" +
                            "\"text\": \"{}\", " +
                            "\"channel\": \"{}\"" +
                            "{}{}" +
                            "}}",
                    SecurityUtils.truncateForLog(notification.message()),
                    notification.channel(),
                    formatUsername(notification.username()),
                    formatIconEmoji(notification.iconEmoji()));

            String messageId = ProviderConstants.Slack.MESSAGE_ID_PREFIX + UUID.randomUUID();
            log.info("[Slack] Response: 200 OK, id: {}", messageId);

            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("[Slack] Failed to send message to channel '{}': {}",
                    notification.channel(), e.getMessage());
            throw new ProviderException(ProviderConstants.Slack.PROVIDER_NAME, "Failed to send Slack message: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.Slack.PROVIDER_NAME;
    }

    private String formatUsername(String username) {
        return username != null ? ", \"username\": \"" + username + "\"" : "";
    }

    private String formatIconEmoji(String iconEmoji) {
        return iconEmoji != null ? ", \"icon_emoji\": \"" + iconEmoji + "\"" : "";
    }

}
