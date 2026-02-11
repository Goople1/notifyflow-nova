package com.nova.notifications.infrastructure.channel.sms.validation;

import com.nova.notifications.common.ValidationMessages;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.SmsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for SMS notifications.
 * <p>
 * Validates phone number format (E.164 international standard),
 * sender identifier, and message content constraints.
 * SMS messages are limited to 1600 characters (Twilio concatenated message limit).
 * </p>
 */
public class SmsValidator implements NotificationValidator<SmsNotification> {

    private static final Logger log = LoggerFactory.getLogger(SmsValidator.class);

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final int MAX_MESSAGE_LENGTH = 1600;

    @Override
    public List<String> validate(SmsNotification notification) {
        log.debug("Validating SMS notification to '{}'", notification.phoneNumber());

        var errors = new ArrayList<String>();

        if (notification.phoneNumber() == null || notification.phoneNumber().isBlank()) {
            errors.add(ValidationMessages.SMS_PHONE_REQUIRED);
        } else if (!E164_PATTERN.matcher(notification.phoneNumber()).matches()) {
            errors.add(ValidationMessages.SMS_PHONE_INVALID_FORMAT + notification.phoneNumber());
        }

        if (notification.from() == null || notification.from().isBlank()) {
            errors.add(ValidationMessages.SMS_SENDER_REQUIRED);
        }

        if (notification.message() == null || notification.message().isBlank()) {
            errors.add(ValidationMessages.SMS_MESSAGE_REQUIRED);
        } else if (notification.message().length() > MAX_MESSAGE_LENGTH) {
            errors.add(ValidationMessages.SMS_MESSAGE_TOO_LONG.formatted(MAX_MESSAGE_LENGTH, notification.message().length()));
        }

        if (!errors.isEmpty()) {
            log.warn("SMS validation failed with {} error(s)", errors.size());
        }

        return errors;
    }
}
