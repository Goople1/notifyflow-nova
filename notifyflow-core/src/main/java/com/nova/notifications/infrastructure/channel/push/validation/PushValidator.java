package com.nova.notifications.infrastructure.channel.push.validation;

import com.nova.notifications.common.SecurityUtils;
import com.nova.notifications.common.ValidationMessages;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for push notifications.
 * <p>
 * Validates the device token (required, minimum 10 characters to cover
 * both FCM registration tokens and APNs device tokens), title, and body.
 * </p>
 */
public class PushValidator implements NotificationValidator<PushNotification> {

    private static final Logger log = LoggerFactory.getLogger(PushValidator.class);

    private static final int MIN_DEVICE_TOKEN_LENGTH = 10;

    @Override
    public List<String> validate(PushNotification notification) {
        log.debug("Validating push notification to device '{}'",
                SecurityUtils.maskToken(notification.deviceToken()));

        var errors = new ArrayList<String>();

        if (notification.deviceToken() == null || notification.deviceToken().isBlank()) {
            errors.add(ValidationMessages.PUSH_TOKEN_REQUIRED);
        } else if (notification.deviceToken().length() < MIN_DEVICE_TOKEN_LENGTH) {
            errors.add(ValidationMessages.PUSH_TOKEN_TOO_SHORT.formatted(MIN_DEVICE_TOKEN_LENGTH));
        }

        if (notification.title() == null || notification.title().isBlank()) {
            errors.add(ValidationMessages.PUSH_TITLE_REQUIRED);
        }

        if (notification.body() == null || notification.body().isBlank()) {
            errors.add(ValidationMessages.PUSH_BODY_REQUIRED);
        }

        if (!errors.isEmpty()) {
            log.warn("Push validation failed with {} error(s)", errors.size());
        }

        return errors;
    }

}
