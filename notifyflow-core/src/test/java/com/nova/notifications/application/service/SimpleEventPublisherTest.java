package com.nova.notifications.application.service;

import com.nova.notifications.application.pubsub.SimpleEventPublisher;
import com.nova.notifications.domain.event.NotificationEvent;
import com.nova.notifications.domain.model.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimpleEventPublisher - Pub/Sub Events")
class SimpleEventPublisherTest {

    private SimpleEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SimpleEventPublisher();
    }

    @Test
    @DisplayName("Should deliver event to subscriber")
    void publishToSubscriber() {
        List<NotificationEvent> received = new ArrayList<>();
        publisher.subscribe(received::add);

        var event = NotificationEvent.queued(ChannelType.EMAIL, "to@test.com");
        publisher.publish(event);

        assertThat(received).hasSize(1);
        assertThat(received.getFirst().eventType()).isEqualTo(NotificationEvent.EventType.QUEUED);
    }

    @Test
    @DisplayName("Should deliver event to multiple subscribers")
    void publishToMultipleSubscribers() {
        List<NotificationEvent> received1 = new ArrayList<>();
        List<NotificationEvent> received2 = new ArrayList<>();
        publisher.subscribe(received1::add);
        publisher.subscribe(received2::add);

        publisher.publish(NotificationEvent.queued(ChannelType.SMS, "+1234567890"));

        assertThat(received1).hasSize(1);
        assertThat(received2).hasSize(1);
    }

    @Test
    @DisplayName("Should not deliver to unsubscribed listener")
    void unsubscribe() {
        List<NotificationEvent> received = new ArrayList<>();
        publisher.subscribe(received::add);
        publisher.unsubscribe(received::add); // Note: won't match lambda identity

        // Use a concrete reference for proper unsubscribe
        var listener = new ArrayList<NotificationEvent>();
        var listenerRef = (com.nova.notifications.application.pubsub.EventListener) listener::add;
        publisher.subscribe(listenerRef);
        publisher.unsubscribe(listenerRef);

        publisher.publish(NotificationEvent.queued(ChannelType.EMAIL, "to@test.com"));

        assertThat(listener).isEmpty();
    }

    @Test
    @DisplayName("Should not let faulty listener block other listeners")
    void listenerExceptionDoesNotAffectOthers() {
        List<NotificationEvent> received = new ArrayList<>();
        publisher.subscribe(event -> { throw new RuntimeException("Faulty listener"); });
        publisher.subscribe(received::add);

        publisher.publish(NotificationEvent.queued(ChannelType.PUSH, "token123"));

        assertThat(received).hasSize(1);
    }
}
