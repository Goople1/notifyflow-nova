package com.nova.notifications.infrastructure.channel.email.validation;

import com.nova.notifications.common.ValidationMessages;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for email notifications.
 * <p>
 * Validates all fields required by email providers: sender address,
 * recipient address, subject, body, and optional CC/BCC lists.
 * All validation errors are collected and returned together.
 * </p>
 */
public class EmailValidator implements NotificationValidator<EmailNotification> {

    private static final Logger log = LoggerFactory.getLogger(EmailValidator.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public List<String> validate(EmailNotification notification) {
        log.debug("Validating email notification to '{}'", notification.to());

        var errors = new ArrayList<String>();

        if (notification.to() == null || notification.to().isBlank()) {
            errors.add(ValidationMessages.EMAIL_RECIPIENT_REQUIRED);
        } else if (!isValidEmail(notification.to())) {
            errors.add(ValidationMessages.EMAIL_RECIPIENT_INVALID_FORMAT + notification.to());
        }

        if (notification.from() == null || notification.from().isBlank()) {
            errors.add(ValidationMessages.EMAIL_SENDER_REQUIRED);
        } else if (!isValidEmail(notification.from())) {
            errors.add(ValidationMessages.EMAIL_SENDER_INVALID_FORMAT + notification.from());
        }

        if (notification.subject() == null || notification.subject().isBlank()) {
            errors.add(ValidationMessages.EMAIL_SUBJECT_REQUIRED);
        }

        if (notification.body() == null || notification.body().isBlank()) {
            errors.add(ValidationMessages.EMAIL_BODY_REQUIRED);
        }

        for (String cc : notification.cc()) {
            if (!isValidEmail(cc)) {
                errors.add(ValidationMessages.EMAIL_CC_INVALID_FORMAT + cc);
            }
        }

        for (String bcc : notification.bcc()) {
            if (!isValidEmail(bcc)) {
                errors.add(ValidationMessages.EMAIL_BCC_INVALID_FORMAT + bcc);
            }
        }

        if (!errors.isEmpty()) {
            log.warn("Email validation failed with {} error(s)", errors.size());
        }

        return errors;
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
