package com.nova.notifications.application.retry;

import com.nova.notifications.common.ErrorSource;
import com.nova.notifications.application.pubsub.EventPublisher;
import com.nova.notifications.application.service.NotificationService;
import com.nova.notifications.domain.event.NotificationEvent;
import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.result.NotificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that adds retry logic with exponential backoff to NotificationService.
 * <p>
 * Wraps a NotificationService and retries failed sends according to the
 * configured RetryPolicy. Only retries provider errors, not validation errors
 * (which would fail again on retry). Publishes RETRYING events for observability.
 * </p>
 */
public class RetryableNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RetryableNotificationService.class);

    private final NotificationService delegate;
    private final RetryPolicy retryPolicy;
    private final EventPublisher eventPublisher;

    public RetryableNotificationService(NotificationService delegate, RetryPolicy retryPolicy,
                                        EventPublisher eventPublisher) {
        this.delegate = delegate;
        this.retryPolicy = retryPolicy;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Sends a notification with automatic retry on provider failures.
     * <p>
     * Validation errors are returned immediately without retry.
     * Provider and system errors trigger retries up to maxAttempts.
     * </p>
     *
     * @param notification the notification to send
     * @return the final result after all attempts
     */
    public <T extends Notification> NotificationResult sendWithRetry(T notification) {
        NotificationResult result = null;

        for (int attempt = 1; attempt <= retryPolicy.maxAttempts(); attempt++) {
            if (attempt > 1) {
                var delay = retryPolicy.delayForAttempt(attempt - 1);
                log.info("Retry attempt {}/{} for {} notification to {} (delay: {}ms)",
                        attempt, retryPolicy.maxAttempts(), notification.channelType(),
                        notification.recipient(), delay.toMillis());
                eventPublisher.publish(
                        NotificationEvent.retrying(notification.channelType(), notification.recipient(), attempt)
                );
                sleep(delay.toMillis());
            }

            result = delegate.send(notification);

            if (result.successful()) {
                return result;
            }

            // Don't retry validation errors - they'll fail again
            if (result.errorSource() != null && result.errorSource().equals(ErrorSource.VALIDATION)) {
                log.debug("Validation error - not retrying: {}", result.errorMessage());
                return result;
            }

            if (attempt < retryPolicy.maxAttempts()) {
                log.warn("Attempt {}/{} failed for {} to {}: {}",
                        attempt, retryPolicy.maxAttempts(), notification.channelType(),
                        notification.recipient(), result.errorMessage());
            }
        }

        log.error("All {} retry attempts exhausted for {} notification to {}",
                retryPolicy.maxAttempts(), notification.channelType(), notification.recipient());
        return result;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Retry sleep interrupted");
        }
    }
}
