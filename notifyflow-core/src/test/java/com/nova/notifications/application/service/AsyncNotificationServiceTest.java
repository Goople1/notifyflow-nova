package com.nova.notifications.application.service;

import com.nova.notifications.application.async.AsyncNotificationService;
import com.nova.notifications.application.port.NotificationChannel;
import com.nova.notifications.application.pubsub.SimpleEventPublisher;
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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsyncNotificationService - Async & Batch")
class AsyncNotificationServiceTest {

    @Mock
    private NotificationChannel<EmailNotification> emailChannel;

    @Mock
    private NotificationChannel<SmsNotification> smsChannel;

    private AsyncNotificationService asyncService;

    @BeforeEach
    void setUp() {
        lenient().when(emailChannel.isAvailable()).thenReturn(true);
        lenient().when(smsChannel.isAvailable()).thenReturn(true);

        Map<ChannelType, NotificationChannel<?>> channels = new EnumMap<>(ChannelType.class);
        channels.put(ChannelType.EMAIL, emailChannel);
        channels.put(ChannelType.SMS, smsChannel);

        var eventPublisher = new SimpleEventPublisher();
        var notificationService = new NotificationService(channels, eventPublisher);

        // Direct executor for deterministic tests
        asyncService = new AsyncNotificationService(notificationService, Runnable::run);
    }

    @Test
    @DisplayName("Should resolve CompletableFuture with success result")
    void sendAsyncSuccess() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        when(emailChannel.send(any())).thenReturn(NotificationResult.success("async-123"));

        var result = asyncService.sendAsync(email).join();

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).isEqualTo("async-123");
    }

    @Test
    @DisplayName("Should resolve CompletableFuture with failure result")
    void sendAsyncFailure() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        when(emailChannel.send(any())).thenReturn(NotificationResult.providerError("SendGrid", "Timeout", null));

        var result = asyncService.sendAsync(email).join();

        assertThat(result.successful()).isFalse();
        assertThat(result.errorMessage()).contains("Timeout");
    }

    @Test
    @DisplayName("Should process batch with mixed success and failure results")
    void sendBatchMixed() {
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello");

        when(emailChannel.send(any())).thenReturn(NotificationResult.success("email-ok"));
        when(smsChannel.send(any())).thenReturn(NotificationResult.providerError("Twilio", "Rate limited", null));

        var results = asyncService.sendBatch(List.of(email, sms)).join();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).successful()).isTrue();
        assertThat(results.get(1).successful()).isFalse();
    }

    @Test
    @DisplayName("Should process batch with all successes")
    void sendBatchAllSuccess() {
        var email1 = EmailNotification.simple("from@test.com", "to1@test.com", "Subject 1", "Body 1");
        var email2 = EmailNotification.simple("from@test.com", "to2@test.com", "Subject 2", "Body 2");

        when(emailChannel.send(any())).thenReturn(NotificationResult.success("batch-ok"));

        var results = asyncService.sendBatch(List.of(email1, email2)).join();

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(NotificationResult::successful);
    }
}
