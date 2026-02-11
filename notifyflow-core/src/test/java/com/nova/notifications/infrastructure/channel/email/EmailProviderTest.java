package com.nova.notifications.infrastructure.channel.email;

import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.infrastructure.channel.email.provider.MailgunProvider;
import com.nova.notifications.infrastructure.channel.email.provider.SendGridProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Email Providers - SendGrid & Mailgun")
class EmailProviderTest {

    @Test
    @DisplayName("SendGrid should return success with 'sg-' prefixed ID")
    void sendGridSendsSuccessfully() {
        var provider = new SendGridProvider("test-api-key");
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = provider.send(email);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("sg-");
        assertThat(provider.getProviderName()).isEqualTo("SendGrid");
    }

    @Test
    @DisplayName("Mailgun should return success with 'mg-' prefixed ID")
    void mailgunSendsSuccessfully() {
        var provider = new MailgunProvider("test-api-key", "test.domain.com");
        var email = EmailNotification.simple("from@test.com", "to@test.com", "Subject", "Body");

        var result = provider.send(email);

        assertThat(result.successful()).isTrue();
        assertThat(result.notificationId()).startsWith("mg-");
        assertThat(provider.getProviderName()).isEqualTo("Mailgun");
    }
}
