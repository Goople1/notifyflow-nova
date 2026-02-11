package com.nova.notifications.domain.model;

import java.util.Map;

/**
 * Notification for the Push channel.
 * <p>
 * Models the data required by push providers like FCM and APNs:
 * device token, title, body, optional data payload, badge count, and sound.
 * </p>
 *
 * @param deviceToken target device registration token
 * @param title       notification title displayed to user
 * @param body        notification body message
 * @param data        custom key-value payload for the app (may be empty)
 * @param badge       badge count for iOS (null to not change)
 * @param sound       notification sound name (null for default)
 */
public record PushNotification(
        String deviceToken,
        String title,
        String body,
        Map<String, String> data,
        Integer badge,
        String sound
) implements Notification {

    public PushNotification {
        data = data != null ? Map.copyOf(data) : Map.of();
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.PUSH;
    }

    @Override
    public String recipient() {
        return deviceToken;
    }

    /**
     * Convenience factory for simple push notifications.
     */
    public static PushNotification simple(String deviceToken, String title, String body) {
        return new PushNotification(deviceToken, title, body, Map.of(), null, null);
    }

    /**
     * Convenience factory with data payload.
     */
    public static PushNotification withData(String deviceToken, String title, String body, Map<String, String> data) {
        return new PushNotification(deviceToken, title, body, data, null, null);
    }
}
