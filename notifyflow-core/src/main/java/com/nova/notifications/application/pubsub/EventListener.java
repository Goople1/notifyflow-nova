package com.nova.notifications.application.pubsub;

import com.nova.notifications.domain.event.NotificationEvent;

/**
 * Listener interface for notification lifecycle events.
 * <p>
 * Consumers implement this to react to notification state changes
 * (sent, failed, retrying, etc.) for logging, metrics, or alerting.
 * </p>
 */
@FunctionalInterface
public interface EventListener {

    /**
     * Called when a notification event occurs.
     *
     * @param event the notification event
     */
    void onEvent(NotificationEvent event);
}
