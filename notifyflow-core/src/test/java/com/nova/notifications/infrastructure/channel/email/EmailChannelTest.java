package com.nova.notifications.infrastructure.channel.email;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.EmailNotification;
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
@DisplayName("EmailChannel - Template Method Flow")
class EmailChannelTest {

    @Mock
    private NotificationProvider<EmailNotification> provider;

    @Mock
    private NotificationValidator<EmailNotification> validator;

    @Test
    @DisplayName("Should send valid email successfully")
    void sendValidEmail() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenReturn(NotificationResult.success("sg-123"));

        var channel = new EmailChannel(provider, validator);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = channel.send(email);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).isEqualTo("sg-123");
        verify(provider).send(email);
    }

    @Test
    @DisplayName("Should return validation error without calling provider")
    void sendInvalidEmail() {
        when(validator.validate(any())).thenReturn(List.of("Invalid email format", "Subject is required"));

        var channel = new EmailChannel(provider, validator);
        var email = EmailNotification.simple("bad", "", "", "");

        var result = channel.send(email);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
        assertThat(result.errorMessage()).contains("Invalid email format");
        verify(provider, never()).send(any());
    }

    @Test
    @DisplayName("Should handle provider exception gracefully")
    void providerThrowsException() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenThrow(new RuntimeException("API unavailable"));
        when(provider.getProviderName()).thenReturn("SendGrid");

        var channel = new EmailChannel(provider, validator);
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = channel.send(email);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).contains("PROVIDER");
    }
}
