package com.nova.notifications.infrastructure.channel.push.provider;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.domain.exception.ProviderException;
import com.nova.notifications.domain.model.PushNotification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.ProviderConstants;
import com.nova.notifications.common.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulated Firebase Cloud Messaging (FCM) push provider.
 * <p>
 * Simulates the FCM HTTP v1 API ({@code POST /v1/projects/{project}/messages:send}).
 * Logs the message structure that would be sent, including token, notification
 * payload (title, body), and optional data payload.
 * </p>
 * <p>
 * Server key is never logged or exposed.
 * </p>
 */
public class FcmProvider implements NotificationProvider<PushNotification> {

    private static final Logger log = LoggerFactory.getLogger(FcmProvider.class);

    private final String serverKey;

    /**
     * Creates a new FCM provider.
     *
     * @param serverKey the FCM server key (never logged)
     */
    public FcmProvider(String serverKey) {
        if (serverKey == null || serverKey.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Fcm.SERVER_KEY_ERROR);
        }
        this.serverKey = serverKey;
        log.info("FCM provider initialized with server key [***]");
    }

    @Override
    public NotificationResult send(PushNotification notification) {
        try {
            log.info("[FCM] Simulating POST /v1/projects/-/messages:send");
            log.info("[FCM] Authorization: Bearer [***]");
            log.info("[FCM] Request body: {{" +
                            "\"message\": {{" +
                            "\"token\": \"{}\", " +
                            "\"notification\": {{\"title\": \"{}\", \"body\": \"{}\"}}" +
                            "{}}}}}",
                    SecurityUtils.maskToken(notification.deviceToken()),
                    notification.title(),
                    SecurityUtils.truncateForLog(notification.body()),
                    formatDataPayload(notification));

            String messageId = ProviderConstants.Fcm.MESSAGE_ID_PREFIX + UUID.randomUUID();
            log.info("[FCM] Response: 200 OK, name: {}", messageId);

            return NotificationResult.success(messageId);
        } catch (Exception e) {
            log.error("[FCM] Failed to send push to device: {}", e.getMessage());
            throw new ProviderException(ProviderConstants.Fcm.PROVIDER_NAME, "Failed to send push notification: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.Fcm.PROVIDER_NAME;
    }

    private String formatDataPayload(PushNotification notification) {
        if (notification.data().isEmpty()) {
            return "";
        }
        var sb = new StringBuilder(", \"data\": {");
        var entries = notification.data().entrySet().iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            sb.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            if (entries.hasNext()) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
