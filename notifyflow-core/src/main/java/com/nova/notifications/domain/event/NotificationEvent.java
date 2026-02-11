package com.nova.notifications.domain.event;

import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.result.NotificationResult;

import java.time.Instant;

/**
 * Domain event representing a notification lifecycle state change.
 * <p>
 * Used by the Pub/Sub system to notify listeners about notification
 * processing status (QUEUED, SENDING, SENT, FAILED, RETRYING).
 * </p>
 *
 * @param eventType   the lifecycle state
 * @param channelType which channel the notification belongs to
 * @param recipient   the notification recipient
 * @param result      the result if available (null for QUEUED/SENDING states)
 * @param attempt     retry attempt number (1 for first try)
 * @param timestamp   when the event occurred
 */
public record NotificationEvent(
        EventType eventType,
        ChannelType channelType,
        String recipient,
        NotificationResult result,
        int attempt,
        Instant timestamp
) {

    public enum EventType {
        QUEUED,
        SENDING,
        SENT,
        FAILED,
        RETRYING
    }

    /**
     * Returns a human-readable description of this event using switch expression.
     */
    public String describe() {
        return switch (eventType) {
            case QUEUED -> "Notification queued for %s to %s".formatted(channelType, recipient);
            case SENDING -> "Sending %s notification to %s (attempt %d)".formatted(channelType, recipient, attempt);
            case SENT -> "Successfully sent %s notification to %s".formatted(channelType, recipient);
            case FAILED -> "Failed to send %s notification to %s (attempt %d)".formatted(channelType, recipient, attempt);
            case RETRYING -> "Retrying %s notification to %s (attempt %d)".formatted(channelType, recipient, attempt);
        };
    }

    public static NotificationEvent queued(ChannelType channelType, String recipient) {
        return new NotificationEvent(EventType.QUEUED, channelType, recipient, null, 0, Instant.now());
    }

    public static NotificationEvent sending(ChannelType channelType, String recipient, int attempt) {
        return new NotificationEvent(EventType.SENDING, channelType, recipient, null, attempt, Instant.now());
    }

    public static NotificationEvent sent(ChannelType channelType, String recipient, NotificationResult result) {
        return new NotificationEvent(EventType.SENT, channelType, recipient, result, 1, Instant.now());
    }

    public static NotificationEvent failed(ChannelType channelType, String recipient, NotificationResult result, int attempt) {
        return new NotificationEvent(EventType.FAILED, channelType, recipient, result, attempt, Instant.now());
    }

    public static NotificationEvent retrying(ChannelType channelType, String recipient, int attempt) {
        return new NotificationEvent(EventType.RETRYING, channelType, recipient, null, attempt, Instant.now());
    }
}
