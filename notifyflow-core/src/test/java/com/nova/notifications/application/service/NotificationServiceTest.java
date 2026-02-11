package com.nova.notifications.application.service;

import com.nova.notifications.application.port.NotificationChannel;
import com.nova.notifications.application.pubsub.EventListener;
import com.nova.notifications.application.pubsub.SimpleEventPublisher;
import com.nova.notifications.domain.event.NotificationEvent;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.domain.result.NotificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService - Routing & Dispatch")
class NotificationServiceTest {

    @Mock
    private NotificationChannel<EmailNotification> emailChannel;

    @Mock
    private NotificationChannel<SmsNotification> smsChannel;

    private SimpleEventPublisher eventPublisher;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        eventPublisher = new SimpleEventPublisher();
        lenient().when(emailChannel.isAvailable()).thenReturn(true);

        Map<ChannelType, NotificationChannel<?>> channels = new EnumMap<>(ChannelType.class);
        channels.put(ChannelType.EMAIL, emailChannel);
        channels.put(ChannelType.SMS, smsChannel);

        service = new NotificationService(channels, eventPublisher);
    }

    @Test
    @DisplayName("Should route email notification to email channel and return success")
    void sendEmailSuccess() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        when(emailChannel.send(any())).thenReturn(NotificationResult.success("msg-123"));

        var result = service.send(email);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).isEqualTo("msg-123");
        verify(emailChannel).send(email);
    }

    @Test
    @DisplayName("Should route SMS notification to SMS channel")
    void sendSmsSuccess() {
        when(smsChannel.isAvailable()).thenReturn(true);
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello SMS");
        when(smsChannel.send(any())).thenReturn(NotificationResult.success("sms-456"));

        var result = service.send(sms);

        assertThat(result.successful()).isTrue();
        verify(smsChannel).send(sms);
    }

    @Test
    @DisplayName("Should return validation error when notification is null")
    void sendNullNotification() {
        var result = service.send(null);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
        assertThat(result.errorMessage()).contains("null");
    }

    @Test
    @DisplayName("Should return configuration error for unconfigured channel type")
    void sendUnconfiguredChannel() {
        var push = com.nova.notifications.domain.model.PushNotification.simple("token12345678", "Title", "Body");

        var result = service.send(push);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("CONFIGURATION");
        assertThat(result.errorMessage()).contains("PUSH");
    }

    @Test
    @DisplayName("Should return configuration error when channel is not available")
    void sendUnavailableChannel() {
        when(emailChannel.isAvailable()).thenReturn(false);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = service.send(email);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("CONFIGURATION");
        verify(emailChannel, never()).send(any());
    }

    @Test
    @DisplayName("Should return system error when channel throws unexpected exception")
    void sendChannelThrowsException() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        when(emailChannel.send(any())).thenThrow(new RuntimeException("Connection timeout"));

        var result = service.send(email);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("SYSTEM");
        assertThat(result.errorMessage()).contains("Connection timeout");
    }

    @Test
    @DisplayName("Should publish SENDING and SENT events on successful send")
    void publishesEventsOnSuccess() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        when(emailChannel.send(any())).thenReturn(NotificationResult.success("msg-789"));

        List<NotificationEvent> capturedEvents = new ArrayList<>();
        eventPublisher.subscribe(capturedEvents::add);

        service.send(email);

        assertThat(capturedEvents).hasSize(2);
        assertThat(capturedEvents.get(0).eventType()).isEqualTo(NotificationEvent.EventType.SENDING);
        assertThat(capturedEvents.get(1).eventType()).isEqualTo(NotificationEvent.EventType.SENT);
    }

    @Test
    @DisplayName("Should publish SENDING and FAILED events on failed send")
    void publishesEventsOnFailure() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        when(emailChannel.send(any())).thenReturn(NotificationResult.providerError("SendGrid", "API error", null));

        List<NotificationEvent> capturedEvents = new ArrayList<>();
        eventPublisher.subscribe(capturedEvents::add);

        service.send(email);

        assertThat(capturedEvents).hasSize(2);
        assertThat(capturedEvents.get(0).eventType()).isEqualTo(NotificationEvent.EventType.SENDING);
        assertThat(capturedEvents.get(1).eventType()).isEqualTo(NotificationEvent.EventType.FAILED);
    }
}
