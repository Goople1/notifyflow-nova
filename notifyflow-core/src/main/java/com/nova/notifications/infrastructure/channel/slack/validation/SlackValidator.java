package com.nova.notifications.infrastructure.channel.slack.validation;

import com.nova.notifications.common.ValidationMessages;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.SlackNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for Slack notifications.
 * <p>
 * Validates the required fields for Slack Incoming Webhooks:
 * channel identifier and message text.
 * </p>
 */
public class SlackValidator implements NotificationValidator<SlackNotification> {

    private static final Logger log = LoggerFactory.getLogger(SlackValidator.class);

    @Override
    public List<String> validate(SlackNotification notification) {
        log.debug("Validating Slack notification to channel '{}'", notification.channel());

        var errors = new ArrayList<String>();

        if (notification.channel() == null || notification.channel().isBlank()) {
            errors.add(ValidationMessages.SLACK_CHANNEL_REQUIRED);
        }

        if (notification.message() == null || notification.message().isBlank()) {
            errors.add(ValidationMessages.SLACK_MESSAGE_REQUIRED);
        }

        if (!errors.isEmpty()) {
            log.warn("Slack validation failed with {} error(s)", errors.size());
        }

        return errors;
    }
}
