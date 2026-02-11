package com.nova.notifications.application.pubsub;

import com.nova.notifications.domain.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory implementation of the EventPublisher.
 * <p>
 * Uses CopyOnWriteArrayList for safe concurrent iteration during event
 * publishing. Listener exceptions are caught and logged to prevent
 * one faulty listener from blocking others.
 * </p>
 */
public class SimpleEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimpleEventPublisher.class);

    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(NotificationEvent event) {
        log.debug("Publishing event: {} for {} to {}", event.eventType(), event.channelType(), event.recipient());
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.warn("Event listener threw exception for event {}: {}", event.eventType(), e.getMessage());
            }
        }
    }

    @Override
    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }
}
