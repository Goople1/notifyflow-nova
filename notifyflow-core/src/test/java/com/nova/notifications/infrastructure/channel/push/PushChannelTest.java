package com.nova.notifications.infrastructure.channel.push;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.PushNotification;
import com.nova.notifications.domain.result.NotificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushChannel - Template Method Flow")
class PushChannelTest {

    @Mock
    private NotificationProvider<PushNotification> provider;

    @Mock
    private NotificationValidator<PushNotification> validator;

    @Test
    @DisplayName("Should send valid push notification successfully")
    void sendValidPush() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenReturn(NotificationResult.success("fcm-123"));

        var channel = new PushChannel(provider, validator);
        var push = PushNotification.simple("device-token-1234567890", "Title", "Body");

        var result = channel.send(push);

        assertThat(result.successful()).isTrue();
        verify(provider).send(push);
    }

    @Test
    @DisplayName("Should return validation error for invalid push")
    void sendInvalidPush() {
        when(validator.validate(any())).thenReturn(List.of("Device token too short"));

        var channel = new PushChannel(provider, validator);
        var push = PushNotification.simple("short", "Title", "Body");

        var result = channel.send(push);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
        verify(provider, never()).send(any());
    }

    @Test
    @DisplayName("Should handle provider failure gracefully")
    void providerFailure() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenThrow(new RuntimeException("FCM unavailable"));
        when(provider.getProviderName()).thenReturn("FCM");

        var channel = new PushChannel(provider, validator);
        var push = PushNotification.simple("device-token-1234567890", "Title", "Body");

        var result = channel.send(push);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).contains("PROVIDER");
    }
}
