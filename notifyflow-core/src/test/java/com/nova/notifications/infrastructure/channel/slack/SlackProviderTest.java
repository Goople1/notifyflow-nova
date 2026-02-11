package com.nova.notifications.infrastructure.channel.slack;

import com.nova.notifications.domain.model.SlackNotification;
import com.nova.notifications.infrastructure.channel.slack.provider.SlackWebhookProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Slack Provider - SlackWebhookProvider")
class SlackProviderTest {

    @Test
    @DisplayName("Should return success with 'slack-' prefixed ID")
    void slackWebhookSendsSuccessfully() {
        var provider = new SlackWebhookProvider("https://hooks.slack.com/services/T00/B00/xxx");
        var slack = new SlackNotification("#general", "Hello team!", "Bot", ":wave:");

        var result = provider.send(slack);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("slack-");
        assertThat(provider.getProviderName()).isEqualTo("Slack");
    }

    @Test
    @DisplayName("Should reject null webhook URL")
    void rejectNullWebhookUrl() {
        assertThatThrownBy(() -> new SlackWebhookProvider(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject blank webhook URL")
    void rejectBlankWebhookUrl() {
        assertThatThrownBy(() -> new SlackWebhookProvider("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
