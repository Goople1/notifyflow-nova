package com.nova.notifications.infrastructure.channel.slack;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.SlackNotification;
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
@DisplayName("SlackChannel - Template Method Flow")
class SlackChannelTest {

    @Mock
    private NotificationProvider<SlackNotification> provider;

    @Mock
    private NotificationValidator<SlackNotification> validator;

    @Test
    @DisplayName("Should send valid Slack message successfully")
    void sendValidSlack() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenReturn(NotificationResult.success("slack-123"));

        var channel = new SlackChannel(provider, validator);
        var slack = SlackNotification.simple("#general", "Hello Slack!");

        var result = channel.send(slack);

        assertThat(result.successful()).isTrue();
        verify(provider).send(slack);
    }

    @Test
    @DisplayName("Should return validation error for invalid Slack message")
    void sendInvalidSlack() {
        when(validator.validate(any())).thenReturn(List.of("Channel is required"));

        var channel = new SlackChannel(provider, validator);
        var slack = SlackNotification.simple(null, "Hello");

        var result = channel.send(slack);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
        verify(provider, never()).send(any());
    }

    @Test
    @DisplayName("Should handle provider failure gracefully")
    void providerFailure() {
        when(validator.validate(any())).thenReturn(List.of());
        when(provider.send(any())).thenThrow(new RuntimeException("Webhook unreachable"));
        when(provider.getProviderName()).thenReturn("Slack");

        var channel = new SlackChannel(provider, validator);
        var slack = SlackNotification.simple("#general", "Hello");

        var result = channel.send(slack);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).contains("PROVIDER");
    }
}
