package com.nova.notifications.infrastructure.channel.sms;

import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.infrastructure.channel.sms.validation.SmsValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SmsValidator - Phone & Message Validation")
class SmsValidatorTest {

    private final SmsValidator validator = new SmsValidator();

    @Test
    @DisplayName("Should pass validation for valid E.164 number and message")
    void validSms() {
        var sms = new SmsNotification("+15551234567", "+15559876543", "Hello SMS");

        var errors = validator.validate(sms);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should reject phone number without '+' prefix")
    void invalidPhoneNoPlus() {
        var sms = new SmsNotification("+15551234567", "15559876543", "Hello");

        var errors = validator.validate(sms);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("phone") || e.toLowerCase().contains("e.164"));
    }

    @Test
    @DisplayName("Should reject too short phone number")
    void phoneTooShort() {
        var sms = new SmsNotification("+15551234567", "+123", "Hello");

        var errors = validator.validate(sms);

        assertThat(errors).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject missing message")
    void missingMessage() {
        var sms = new SmsNotification("+15551234567", "+15559876543", null);

        var errors = validator.validate(sms);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("message"));
    }

    @Test
    @DisplayName("Should reject message exceeding 1600 characters")
    void messageTooLong() {
        var longMessage = "x".repeat(1601);
        var sms = new SmsNotification("+15551234567", "+15559876543", longMessage);

        var errors = validator.validate(sms);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("1600") || e.toLowerCase().contains("long"));
    }
}
