package com.nova.notifications.infrastructure.channel.sms;

import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.infrastructure.channel.sms.provider.TwilioProvider;
import com.nova.notifications.infrastructure.channel.sms.provider.VonageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SMS Providers - Twilio & Vonage")
class SmsProviderTest {

    @Test
    @DisplayName("Twilio should return success with 'SM' prefixed ID")
    void twilioSendsSuccessfully() {
        var provider = new TwilioProvider("test-sid", "test-token");
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello");

        var result = provider.send(sms);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("SM");
        assertThat(provider.getProviderName()).isEqualTo("Twilio");
    }

    @Test
    @DisplayName("Vonage should return success with 'vonage-' prefixed ID")
    void vonageSendsSuccessfully() {
        var provider = new VonageProvider("test-key", "test-secret");
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello");

        var result = provider.send(sms);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("vonage-");
        assertThat(provider.getProviderName()).isEqualTo("Vonage");
    }
}
