package com.nova.notifications.infrastructure.config;

import com.nova.notifications.application.async.AsyncNotificationService;
import com.nova.notifications.application.pubsub.EventListener;
import com.nova.notifications.application.pubsub.EventPublisher;
import com.nova.notifications.application.retry.RetryableNotificationService;
import com.nova.notifications.application.service.NotificationService;
import com.nova.notifications.application.template.TemplateRegistry;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.result.NotificationResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main facade for the NotifyFlow notification library.
 * <p>
 * Provides a unified, simple API for sending notifications through any configured
 * channel — synchronously, asynchronously, with retries, or in batch.
 * </p>
 *
 * <h3>Quick start:</h3>
 * <pre>{@code
 * var notifyFlow = NotifyFlowBuilder.create()
 *     .withSendGrid("api-key")
 *     .withTwilio("sid", "token")
 *     .withFcm("server-key")
 *     .build();
 *
 * // Sync send
 * var result = notifyFlow.send(EmailNotification.simple("from@x.com", "to@x.com", "Hi", "Hello!"));
 *
 * // Async send
 * notifyFlow.sendAsync(SmsNotification(...)).thenAccept(r -> ...);
 *
 * // Batch send
 * notifyFlow.sendBatch(List.of(email, sms, push)).thenAccept(results -> ...);
 * }</pre>
 */
public class NotifyFlow {

    private final NotificationService notificationService;
    private final RetryableNotificationService retryService;
    private final AsyncNotificationService asyncService;
    private final TemplateRegistry templateRegistry;
    private final EventPublisher eventPublisher;

    NotifyFlow(NotificationService notificationService,
               RetryableNotificationService retryService,
               AsyncNotificationService asyncService,
               TemplateRegistry templateRegistry,
               EventPublisher eventPublisher) {
        this.notificationService = notificationService;
        this.retryService = retryService;
        this.asyncService = asyncService;
        this.templateRegistry = templateRegistry;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Sends a notification synchronously through the appropriate channel.
     *
     * @param notification the notification to send
     * @return the result of the send attempt
     */
    public <T extends Notification> NotificationResult send(T notification) {
        return notificationService.send(notification);
    }

    /**
     * Sends a notification synchronously with automatic retry on provider failures.
     *
     * @param notification the notification to send
     * @return the final result after all retry attempts
     */
    public <T extends Notification> NotificationResult sendWithRetry(T notification) {
        return retryService.sendWithRetry(notification);
    }

    /**
     * Sends a notification asynchronously.
     *
     * @param notification the notification to send
     * @return a CompletableFuture resolving to the send result
     */
    public <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification) {
        return asyncService.sendAsync(notification);
    }

    /**
     * Sends multiple notifications asynchronously in batch.
     * Fail-soft: one failure does not cancel others.
     *
     * @param notifications the notifications to send
     * @return a CompletableFuture resolving to all results
     */
    public CompletableFuture<List<NotificationResult>> sendBatch(List<? extends Notification> notifications) {
        return asyncService.sendBatch(notifications);
    }

    /**
     * Checks if a channel is configured and available.
     */
    public boolean isChannelAvailable(ChannelType channelType) {
        return notificationService.isChannelAvailable(channelType);
    }

    /**
     * Returns the template registry for rendering message templates.
     */
    public TemplateRegistry templates() {
        return templateRegistry;
    }

    /**
     * Renders a registered template with the given variables.
     *
     * @param templateName the template name
     * @param variables    key-value pairs for placeholder replacement
     * @return the rendered string
     */
    public String renderTemplate(String templateName, Map<String, String> variables) {
        return templateRegistry.render(templateName, variables);
    }

    /**
     * Subscribes to notification lifecycle events.
     */
    public void onEvent(EventListener listener) {
        eventPublisher.subscribe(listener);
    }

    /**
     * Creates a new builder for configuring NotifyFlow.
     */
    public static NotifyFlowBuilder builder() {
        return NotifyFlowBuilder.create();
    }
}
