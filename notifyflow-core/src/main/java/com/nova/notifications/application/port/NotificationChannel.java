package com.nova.notifications.application.port;

import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.result.NotificationResult;

/**
 * Port interface defining the contract for a notification channel.
 * <p>
 * Each channel implementation (Email, SMS, Push, Slack) must implement this
 * interface. This is the Strategy pattern entry point: channels are interchangeable
 * strategies for sending notifications.
 * </p>
 * <p>
 * The Template Method flow is: validate → send via provider → return result.
 * Each channel implementation follows this pattern internally.
 * </p>
 *
 * @param <T> the specific notification type this channel handles
 */
public interface NotificationChannel<T extends Notification> {

    /**
     * Sends a notification through this channel.
     *
     * @param notification the notification to send
     * @return the result indicating success or failure with details
     */
    NotificationResult send(T notification);

    /**
     * @return the channel type this implementation handles
     */
    ChannelType getChannelType();

    /**
     * @return true if this channel is properly configured and ready to send
     */
    boolean isAvailable();
}
