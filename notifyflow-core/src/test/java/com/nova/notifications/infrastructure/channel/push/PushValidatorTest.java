package com.nova.notifications.infrastructure.channel.push;

import com.nova.notifications.domain.model.PushNotification;
import com.nova.notifications.infrastructure.channel.push.validation.PushValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PushValidator - Token & Field Validation")
class PushValidatorTest {

    private final PushValidator validator = new PushValidator();

    @Test
    @DisplayName("Should pass validation for valid push notification")
    void validPush() {
        var push = PushNotification.simple("abcdefghij1234567890", "Title", "Body content");

        var errors = validator.validate(push);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should reject device token shorter than 10 characters")
    void shortToken() {
        var push = PushNotification.simple("short", "Title", "Body");

        var errors = validator.validate(push);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("token"));
    }

    @Test
    @DisplayName("Should reject missing title")
    void missingTitle() {
        var push = PushNotification.simple("abcdefghij1234567890", null, "Body");

        var errors = validator.validate(push);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("title"));
    }

    @Test
    @DisplayName("Should reject missing body")
    void missingBody() {
        var push = PushNotification.simple("abcdefghij1234567890", "Title", null);

        var errors = validator.validate(push);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("body"));
    }
}
