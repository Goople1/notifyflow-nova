package com.nova.notifications.application.service;

import com.nova.notifications.application.port.NotificationChannel;
import com.nova.notifications.application.pubsub.SimpleEventPublisher;
import com.nova.notifications.application.retry.RetryPolicy;
import com.nova.notifications.application.retry.RetryableNotificationService;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.domain.result.NotificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryableNotificationService - Retry Logic")
class RetryableNotificationServiceTest {

    @Mock
    private NotificationChannel<EmailNotification> emailChannel;

    private static final RetryPolicy FAST_RETRY = new RetryPolicy(3, Duration.ofMillis(1), 1.0, Duration.ofMillis(1));

    private RetryableNotificationService createRetryService(RetryPolicy policy) {
        when(emailChannel.isAvailable()).thenReturn(true);
        Map<ChannelType, NotificationChannel<?>> channels = new EnumMap<>(ChannelType.class);
        channels.put(ChannelType.EMAIL, emailChannel);

        var publisher = new SimpleEventPublisher();
        var notificationService = new NotificationService(channels, publisher);
        return new RetryableNotificationService(notificationService, policy, publisher);
    }

    @Test
    @DisplayName("Should not retry when first attempt succeeds")
    void noRetryOnSuccess() {
        when(emailChannel.send(any())).thenReturn(NotificationResult.success("ok-1"));
        var retryService = createRetryService(FAST_RETRY);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = retryService.sendWithRetry(email);

        assertThat(result.successful()).isTrue();
        verify(emailChannel, times(1)).send(any());
    }

    @Test
    @DisplayName("Should retry on provider error and succeed on second attempt")
    void retryOnProviderError() {
        when(emailChannel.send(any()))
                .thenReturn(NotificationResult.providerError("SendGrid", "Timeout", null))
                .thenReturn(NotificationResult.success("ok-retry"));

        var retryService = createRetryService(FAST_RETRY);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = retryService.sendWithRetry(email);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).isEqualTo("ok-retry");
        verify(emailChannel, times(2)).send(any());
    }

    @Test
    @DisplayName("Should NOT retry on validation errors")
    void noRetryOnValidationError() {
        when(emailChannel.send(any())).thenReturn(NotificationResult.validationError("Invalid email format"));
        var retryService = createRetryService(FAST_RETRY);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = retryService.sendWithRetry(email);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
        verify(emailChannel, times(1)).send(any());
    }

    @Test
    @DisplayName("Should exhaust all retry attempts and return last failure")
    void exhaustedRetries() {
        when(emailChannel.send(any()))
                .thenReturn(NotificationResult.providerError("SendGrid", "Error 1", null))
                .thenReturn(NotificationResult.providerError("SendGrid", "Error 2", null))
                .thenReturn(NotificationResult.providerError("SendGrid", "Error 3", null));

        var retryService = createRetryService(FAST_RETRY);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = retryService.sendWithRetry(email);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Error 3");
        verify(emailChannel, times(3)).send(any());
    }
}
