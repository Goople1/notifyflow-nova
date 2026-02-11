package com.nova.notifications.infrastructure.channel.sms;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.SmsNotification;
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
@DisplayName("SmsChannel - Template Method Flow")
class SmsChannelTest {

    @Mock
    private NotificationProvider<SmsNotification> provider;

    @Mock
    private NotificationValidator<SmsNotification> validator;

    @Test
    @DisplayName("Should send valid SMS successfully")
    void sendValidSms() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenReturn(NotificationResult.success("SM123"));

        var channel = new SmsChannel(provider, validator);
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello");

        var result = channel.send(sms);

        assertThat(result.successful()).isTrue();
        verify(provider).send(sms);
    }

    @Test
    @DisplayName("Should return validation error for invalid SMS")
    void sendInvalidSms() {
        when(validator.validate(any())).thenReturn(List.of("Invalid phone number format"));

        var channel = new SmsChannel(provider, validator);
        var sms = new SmsNotification("+1", "123", "Hello");

        var result = channel.send(sms);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
        verify(provider, never()).send(any());
    }

    @Test
    @DisplayName("Should handle provider failure gracefully")
    void providerFailure() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenThrow(new RuntimeException("Service down"));
        when(provider.getProviderName()).thenReturn("Twilio");

        var channel = new SmsChannel(provider, validator);
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello");

        var result = channel.send(sms);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).contains("PROVIDER");
    }
}
