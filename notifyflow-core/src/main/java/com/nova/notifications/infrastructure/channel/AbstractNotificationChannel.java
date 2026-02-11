package com.nova.notifications.infrastructure.channel;

import com.nova.notifications.application.port.NotificationChannel;
import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.result.NotificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Abstract base class implementing the Template Method pattern for all channels.
 * <p>
 * Defines the invariant send flow: validate → send via provider → handle errors.
 * Concrete channels only need to specify their type and provide the logger.
 * This eliminates code duplication across Email, SMS, Push, and Slack channels.
 * </p>
 *
 * @param <T> the specific notification type this channel handles
 */
public abstract class AbstractNotificationChannel<T extends Notification> implements NotificationChannel<T> {

    private final NotificationProvider<T> provider;
    private final NotificationValidator<T> validator;
    private final Logger log;

    protected AbstractNotificationChannel(NotificationProvider<T> provider, NotificationValidator<T> validator) {
        this.provider = provider;
        this.validator = validator;
        this.log = LoggerFactory.getLogger(getClass());
        log.info("{} initialized with provider '{}'", getClass().getSimpleName(), provider.getProviderName());
    }

    /**
     * Template Method: validate → send via provider → handle errors.
     * This flow is invariant across all channels.
     */
    @Override
    public final NotificationResult send(T notification) {
        // Step 1: Validate
        List<String> errors = validator.validate(notification);
        if (!errors.isEmpty()) {
            String joined = String.join("; ", errors);
            log.warn("{} validation failed: {}", getChannelType(), joined);
            return NotificationResult.validationError(joined);
        }

        // Step 2: Send via provider
        try {
            NotificationResult result = provider.send(notification);

            // Step 3: Log based on actual result
            if (result.successful()) {
                log.info("{} sent successfully via {} to '{}', id={}",
                        getChannelType(), provider.getProviderName(),
                        notification.recipient(), result.notificationId());
            } else {
                log.warn("{} send returned failure via {} to '{}': {}",
                        getChannelType(), provider.getProviderName(),
                        notification.recipient(), result.errorMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Unexpected error sending {} via '{}': {}",
                    getChannelType(), provider.getProviderName(), e.getMessage());
            return NotificationResult.providerError(provider.getProviderName(), e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
