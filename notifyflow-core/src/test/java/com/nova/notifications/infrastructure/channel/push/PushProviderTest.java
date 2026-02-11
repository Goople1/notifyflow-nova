package com.nova.notifications.infrastructure.channel.push;

import com.nova.notifications.domain.model.PushNotification;
import com.nova.notifications.infrastructure.channel.push.provider.ApnsProvider;
import com.nova.notifications.infrastructure.channel.push.provider.FcmProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Push Providers - FCM & APNs")
class PushProviderTest {

    @Test
    @DisplayName("FCM should return success with 'projects/-/messages/' prefixed ID")
    void fcmSendsSuccessfully() {
        var provider = new FcmProvider("test-server-key");
        var push = PushNotification.withData("device-token-1234567890", "Alert", "You have a message",
                Map.of("action", "open_chat"));

        var result = provider.send(push);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("projects/-/messages/");
        assertThat(provider.getProviderName()).isEqualTo("FCM");
    }

    @Test
    @DisplayName("APNs should return success with 'apns-' prefixed ID")
    void apnsSendsSuccessfully() {
        var provider = new ApnsProvider("TEAM123", "KEY456", "com.app.bundle");
        var push = PushNotification.simple("device-token-1234567890", "New Message", "Hello from APNs");

        var result = provider.send(push);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("apns-");
        assertThat(provider.getProviderName()).isEqualTo("APNs");
    }
}
