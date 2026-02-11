package com.nova.notifications.infrastructure.channel;

import com.nova.notifications.infrastructure.channel.email.provider.MailgunProvider;
import com.nova.notifications.infrastructure.channel.email.provider.SendGridProvider;
import com.nova.notifications.infrastructure.channel.push.provider.ApnsProvider;
import com.nova.notifications.infrastructure.channel.push.provider.FcmProvider;
import com.nova.notifications.infrastructure.channel.sms.provider.TwilioProvider;
import com.nova.notifications.infrastructure.channel.sms.provider.VonageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Provider Constructor Validation - Credential Rejection")
class ProviderConstructorValidationTest {

    @Test
    @DisplayName("SendGrid should reject null API key")
    void sendGridRejectsNull() {
        assertThatThrownBy(() -> new SendGridProvider(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SendGrid should reject blank API key")
    void sendGridRejectsBlank() {
        assertThatThrownBy(() -> new SendGridProvider("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Mailgun should reject null API key")
    void mailgunRejectsNullKey() {
        assertThatThrownBy(() -> new MailgunProvider(null, "domain.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Mailgun should reject null domain")
    void mailgunRejectsNullDomain() {
        assertThatThrownBy(() -> new MailgunProvider("key", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Twilio should reject null account SID")
    void twilioRejectsNullSid() {
        assertThatThrownBy(() -> new TwilioProvider(null, "token"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Twilio should reject null auth token")
    void twilioRejectsNullToken() {
        assertThatThrownBy(() -> new TwilioProvider("sid", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Vonage should reject null API key")
    void vonageRejectsNull() {
        assertThatThrownBy(() -> new VonageProvider(null, "secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("FCM should reject null server key")
    void fcmRejectsNull() {
        assertThatThrownBy(() -> new FcmProvider(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("APNs should reject null team ID")
    void apnsRejectsNull() {
        assertThatThrownBy(() -> new ApnsProvider(null, "key", "bundle"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
