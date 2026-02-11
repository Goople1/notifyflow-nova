package com.nova.notifications.application.service;

import com.nova.notifications.application.port.NotificationChannel;
import com.nova.notifications.application.pubsub.EventPublisher;
import com.nova.notifications.domain.event.NotificationEvent;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.model.PushNotification;
import com.nova.notifications.domain.model.SlackNotification;
import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.SecurityUtils;
import com.nova.notifications.common.ValidationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Core notification service that routes notifications to the appropriate channel.
 * <p>
 * Acts as a facade for the notification system. Uses an EnumMap for O(1) channel
 * resolution based on the notification's channel type. Publishes lifecycle events
 * through the EventPublisher for observability.
 * </p>
 */
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<ChannelType, NotificationChannel<?>> channels;
    private final EventPublisher eventPublisher;

    public NotificationService(Map<ChannelType, NotificationChannel<?>> channels, EventPublisher eventPublisher) {
        this.channels = new EnumMap<>(channels);
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "EventPublisher must not be null");
    }

    /**
     * Sends a notification through the appropriate channel.
     * <p>
     * Flow: validate channel exists → publish SENDING event → delegate to channel → publish result event.
     * Never throws exceptions to the caller; all errors are captured in NotificationResult.
     * </p>
     *
     * @param notification the notification to send
     * @return the result of the send attempt
     */
    @SuppressWarnings("unchecked")
    public <T extends Notification> NotificationResult send(T notification) {
        if (notification == null) {
            log.warn("Attempted to send null notification");
            return NotificationResult.validationError(ValidationMessages.NOTIFICATION_NULL);
        }

        var channelType = notification.channelType();
        log.debug("Dispatching {}", describeNotification(notification));

        var channel = (NotificationChannel<T>) channels.get(channelType);
        if (channel == null) {
            log.warn("No channel configured for type: {}", channelType);
            return NotificationResult.configurationError(
                    ValidationMessages.NO_CHANNEL_CONFIGURED + channelType
            );
        }

        if (!channel.isAvailable()) {
            log.warn("Channel {} is not available", channelType);
            return NotificationResult.configurationError(
                    ValidationMessages.CHANNEL_NOT_AVAILABLE.formatted(channelType)
            );
        }

        try {
            eventPublisher.publish(NotificationEvent.sending(channelType, notification.recipient(), 1));

            NotificationResult result = channel.send(notification);

            if (result.successful()) {
                eventPublisher.publish(NotificationEvent.sent(channelType, notification.recipient(), result));
                log.info("Successfully sent {}", describeNotification(notification));
            } else {
                eventPublisher.publish(NotificationEvent.failed(channelType, notification.recipient(), result, 1));
                log.warn("Failed to send {} - {}", describeNotification(notification), result.errorMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("Unexpected error sending {}", describeNotification(notification), e);
            var result = NotificationResult.systemError(ValidationMessages.UNEXPECTED_ERROR_PREFIX + e.getMessage(), e);
            eventPublisher.publish(NotificationEvent.failed(channelType, notification.recipient(), result, 1));
            return result;
        }
    }

    /**
     * Checks if a specific channel is configured and available.
     */
    public boolean isChannelAvailable(ChannelType channelType) {
        var channel = channels.get(channelType);
        return channel != null && channel.isAvailable();
    }

    /**
     * Returns a human-readable description of the notification using pattern matching.
     * <p>
     * This switch is exhaustive because {@link Notification} is a sealed interface —
     * no default branch is needed, and the compiler will enforce that all permitted
     * types are handled.
     * </p>
     */
    private <T extends Notification> String describeNotification(T notification) {
        return switch (notification) {
            case EmailNotification e -> "email to '%s' [subject: %s]".formatted(e.to(), e.subject());
            case SmsNotification s -> "SMS to '%s'".formatted(s.phoneNumber());
            case PushNotification p -> "push to device '%s' [title: %s]".formatted(
                    SecurityUtils.maskToken(p.deviceToken()),
                    p.title());
            case SlackNotification sl -> "Slack message to '%s'".formatted(sl.channel());
        };
    }
}
