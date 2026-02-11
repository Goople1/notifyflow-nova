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
 * Simulated Apple Push Notification service (APNs) provider.
 * <p>
 * Simulates the APNs HTTP/2 API ({@code POST /3/device/{deviceToken}}).
 * Logs the JSON payload structure including the {@code aps} dictionary
 * with alert (title, body), badge, and sound fields.
 * </p>
 * <p>
 * Credentials (teamId, keyId, bundleId) are never logged or exposed.
 * </p>
 */
public class ApnsProvider implements NotificationProvider<PushNotification> {

    private static final Logger log = LoggerFactory.getLogger(ApnsProvider.class);

    private final String teamId;
    private final String keyId;
    private final String bundleId;

    /**
     * Creates a new APNs provider.
     *
     * @param teamId   the Apple Developer Team ID (never logged)
     * @param keyId    the APNs authentication key ID (never logged)
     * @param bundleId the application bundle identifier
     */
    public ApnsProvider(String teamId, String keyId, String bundleId) {
        if (teamId == null || teamId.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Apns.TEAM_ID_ERROR);
        }
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Apns.KEY_ID_ERROR);
        }
        if (bundleId == null || bundleId.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Apns.BUNDLE_ID_ERROR);
        }
        this.teamId = teamId;
        this.keyId = keyId;
        this.bundleId = bundleId;
        log.info("APNs provider initialized for bundle '{}' with Team ID [***] and Key ID [***]", bundleId);
    }

    @Override
    public NotificationResult send(PushNotification notification) {
        try {
            log.info("[APNs] Simulating POST /3/device/{}", SecurityUtils.maskToken(notification.deviceToken()));
            log.info("[APNs] Headers: apns-topic={}, authorization=bearer [***]", bundleId);
            log.info("[APNs] Payload: {{\"aps\": {{" +
                            "\"alert\": {{\"title\": \"{}\", \"body\": \"{}\"}}" +
                            "{}{}" +
                            "}}}}",
                    notification.title(),
                    SecurityUtils.truncateForLog(notification.body()),
                    formatBadge(notification.badge()),
                    formatSound(notification.sound()));

            String apnsId = ProviderConstants.Apns.MESSAGE_ID_PREFIX + UUID.randomUUID();
            log.info("[APNs] Response: 200 OK, apns-id: {}", apnsId);

            return NotificationResult.success(apnsId);
        } catch (Exception e) {
            log.error("[APNs] Failed to send push to device: {}", e.getMessage());
            throw new ProviderException(ProviderConstants.Apns.PROVIDER_NAME, "Failed to send push notification: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.Apns.PROVIDER_NAME;
    }

    private String formatBadge(Integer badge) {
        return badge != null ? ", \"badge\": " + badge : "";
    }

    private String formatSound(String sound) {
        return sound != null ? ", \"sound\": \"" + sound + "\"" : "";
    }

}
