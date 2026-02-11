package com.nova.notifications.infrastructure.channel.email;

import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.infrastructure.channel.email.validation.EmailValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailValidator - Email Field Validation")
class EmailValidatorTest {

    private final EmailValidator validator = new EmailValidator();

    @Test
    @DisplayName("Should pass validation for a complete valid email")
    void validEmail() {
        var email = EmailNotification.simple("sender@company.com", "recipient@client.com", "Hello", "World");

        var errors = validator.validate(email);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should reject missing 'to' field")
    void missingTo() {
        var email = new EmailNotification("sender@test.com", null, "Subject", "Body", false, List.of(), List.of());

        var errors = validator.validate(email);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("to") || e.toLowerCase().contains("recipient"));
    }

    @Test
    @DisplayName("Should reject invalid 'to' email format")
    void invalidToFormat() {
        var email = EmailNotification.simple("sender@test.com", "not-an-email", "Subject", "Body");

        var errors = validator.validate(email);

        assertThat(errors).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject missing subject")
    void missingSubject() {
        var email = new EmailNotification("sender@test.com", "to@test.com", null, "Body", false, List.of(), List.of());

        var errors = validator.validate(email);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("subject"));
    }

    @Test
    @DisplayName("Should reject missing body")
    void missingBody() {
        var email = new EmailNotification("sender@test.com", "to@test.com", "Subject", null, false, List.of(), List.of());

        var errors = validator.validate(email);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("body"));
    }

    @Test
    @DisplayName("Should reject invalid CC email")
    void invalidCcEmail() {
        var email = new EmailNotification("sender@test.com", "to@test.com", "Subject", "Body", false,
                List.of("invalid-cc"), List.of());

        var errors = validator.validate(email);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("cc"));
    }

    @Test
    @DisplayName("Should collect multiple errors at once")
    void multipleErrors() {
        var email = new EmailNotification(null, null, null, null, false, List.of(), List.of());

        var errors = validator.validate(email);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(3);
    }
}
