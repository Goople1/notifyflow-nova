package com.nova.notifications.application.pubsub;

import com.nova.notifications.domain.event.NotificationEvent;

/**
 * Publisher interface for notification lifecycle events.
 * <p>
 * Implements the Observer/Pub-Sub pattern to decouple notification
 * sending from event handling (logging, metrics, alerting, etc.).
 * </p>
 */
public interface EventPublisher {

    /**
     * Publishes a notification event to all registered listeners.
     *
     * @param event the event to publish
     */
    void publish(NotificationEvent event);

    /**
     * Registers a listener for notification events.
     *
     * @param listener the listener to register
     */
    void subscribe(EventListener listener);

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     */
    void unsubscribe(EventListener listener);
}
