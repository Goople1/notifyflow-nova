package com.nova.notifications.infrastructure.channel.slack;

import com.nova.notifications.domain.model.SlackNotification;
import com.nova.notifications.infrastructure.channel.slack.validation.SlackValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlackValidator - Channel & Message Validation")
class SlackValidatorTest {

    private final SlackValidator validator = new SlackValidator();

    @Test
    @DisplayName("Should pass validation for valid Slack notification")
    void validSlack() {
        var slack = SlackNotification.simple("#general", "Hello team!");

        var errors = validator.validate(slack);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should reject missing channel")
    void missingChannel() {
        var slack = SlackNotification.simple(null, "Hello");

        var errors = validator.validate(slack);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("channel"));
    }

    @Test
    @DisplayName("Should reject missing message")
    void missingMessage() {
        var slack = SlackNotification.simple("#general", null);

        var errors = validator.validate(slack);

        assertThat(errors).anyMatch(e -> e.toLowerCase().contains("message"));
    }
}
