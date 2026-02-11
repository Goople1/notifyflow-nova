package com.nova.notifications.application.port;

import com.nova.notifications.domain.model.Notification;

import java.util.List;

/**
 * Port interface for notification validation.
 * <p>
 * Each channel has its own validator that checks channel-specific rules
 * (email format, phone number format, device token validity, etc.).
 * Returns a list of all validation errors found, enabling batch error reporting.
 * </p>
 *
 * @param <T> the specific notification type to validate
 */
public interface NotificationValidator<T extends Notification> {

    /**
     * Validates a notification and returns all errors found.
     *
     * @param notification the notification to validate
     * @return list of error messages (empty if valid)
     */
    List<String> validate(T notification);
}
