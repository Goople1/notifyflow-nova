package com.nova.notifications.config;

import com.nova.notifications.domain.model.*;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.infrastructure.channel.email.provider.SendGridProvider;
import com.nova.notifications.infrastructure.channel.push.provider.FcmProvider;
import com.nova.notifications.infrastructure.channel.slack.provider.SlackWebhookProvider;
import com.nova.notifications.infrastructure.channel.sms.provider.TwilioProvider;
import com.nova.notifications.infrastructure.config.NotifyFlowBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NotifyFlowBuilder - Integration Tests")
class NotifyFlowBuilderTest {

    @Test
    @DisplayName("Should build with all four channels configured")
    void buildWithAllChannels() {
        var notifyFlow = NotifyFlowBuilder.create()
                .withSendGrid("sg-api-key")
                .withTwilio("twilio-sid", "twilio-token")
                .withFcm("fcm-server-key")
                .withSlackWebhook("https://hooks.slack.com/test")
                .build();

        assertThat(notifyFlow.isChannelAvailable(ChannelType.EMAIL)).isTrue();
        assertThat(notifyFlow.isChannelAvailable(ChannelType.SMS)).isTrue();
        assertThat(notifyFlow.isChannelAvailable(ChannelType.PUSH)).isTrue();
        assertThat(notifyFlow.isChannelAvailable(ChannelType.SLACK)).isTrue();
    }

    @Test
    @DisplayName("Should throw when building with no channels")
    void buildWithNoChannels() {
        assertThatThrownBy(() -> NotifyFlowBuilder.create().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("channel");
    }

    @Test
    @DisplayName("Should send email through fully built instance")
    void sendThroughBuiltInstance() {
        var notifyFlow = NotifyFlowBuilder.create()
                .withSendGrid("test-key")
                .build();

        var email = EmailNotification.simple("from@test.com", "to@test.com", "Test Subject", "Test Body");
        var result = notifyFlow.send(email);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("sg-");
    }

    @Test
    @DisplayName("Should send SMS through convenience method")
    void sendSmsThroughConvenience() {
        var notifyFlow = NotifyFlowBuilder.create()
                .withTwilio("test-sid", "test-token")
                .build();

        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello SMS");
        var result = notifyFlow.send(sms);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("SM");
    }

    @Test
    @DisplayName("Should register and render templates through builder")
    void templateRegistration() {
        var notifyFlow = NotifyFlowBuilder.create()
                .withSendGrid("test-key")
                .withTemplate("welcome", "Hello {{name}}, welcome!")
                .build();

        var rendered = notifyFlow.renderTemplate("welcome", Map.of("name", "Alice"));

        assertThat(rendered).isEqualTo("Hello Alice, welcome!");
    }

    @Test
    @DisplayName("Should return configuration error for unconfigured channel")
    void unconfiguredChannelReturnsError() {
        var notifyFlow = NotifyFlowBuilder.create()
                .withSendGrid("test-key")
                .build();

        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello");
        var result = notifyFlow.send(sms);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("CONFIGURATION");
    }

    @Test
    @DisplayName("Should return validation error for invalid notification data")
    void validationErrorThroughBuilder() {
        var notifyFlow = NotifyFlowBuilder.create()
                .withSendGrid("test-key")
                .build();

        var invalidEmail = EmailNotification.simple("", "not-an-email", "", "");
        var result = notifyFlow.send(invalidEmail);

        assertThat(result.successful()).isFalse();
        assertThat(result.errorSource()).isEqualTo("VALIDATION");
    }
}
