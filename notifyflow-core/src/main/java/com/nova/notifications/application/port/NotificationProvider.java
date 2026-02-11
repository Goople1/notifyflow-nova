package com.nova.notifications.application.port;

import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.result.NotificationResult;

/**
 * Port interface for notification providers (external services).
 * <p>
 * Each provider represents a concrete external service integration
 * (SendGrid, Mailgun, Twilio, Vonage, FCM, APNs, Slack Webhooks).
 * Providers are the innermost strategy that channels delegate to.
 * </p>
 * <p>
 * In this library, providers simulate the actual API calls.
 * However, the interface is designed so that real HTTP implementations
 * can be plugged in without changing any other code.
 * </p>
 *
 * @param <T> the specific notification type this provider handles
 */
public interface NotificationProvider<T extends Notification> {

    /**
     * Sends a notification through the external provider.
     *
     * @param notification the notification to send
     * @return the result from the provider
     */
    NotificationResult send(T notification);

    /**
     * @return the provider name (e.g., "SendGrid", "Twilio", "FCM")
     */
    String getProviderName();
}
