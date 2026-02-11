package com.nova.notifications.application.async;

import com.nova.notifications.application.service.NotificationService;
import com.nova.notifications.domain.model.Notification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.ValidationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Asynchronous wrapper around NotificationService.
 * <p>
 * Provides non-blocking notification sending using CompletableFuture
 * and a configurable Executor. Supports single async sends and batch
 * processing with fail-soft semantics (one failure doesn't abort others).
 * </p>
 */
public class AsyncNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncNotificationService.class);

    private final NotificationService notificationService;
    private final Executor executor;

    public AsyncNotificationService(NotificationService notificationService, Executor executor) {
        this.notificationService = Objects.requireNonNull(notificationService, "NotificationService must not be null");
        this.executor = Objects.requireNonNull(executor, "Executor must not be null");
    }

    /**
     * Sends a single notification asynchronously.
     *
     * @param notification the notification to send
     * @return a CompletableFuture that resolves to the send result
     */
    public <T extends Notification> CompletableFuture<NotificationResult> sendAsync(T notification) {
        log.debug("Queueing async {} notification to: {}", notification.channelType(), notification.recipient());
        return CompletableFuture.supplyAsync(
                () -> notificationService.send(notification),
                executor
        ).exceptionally(ex -> {
            log.error("Async notification failed for {}: {}", notification.recipient(), ex.getMessage(), ex);
            return NotificationResult.systemError(ValidationMessages.ASYNC_ERROR_PREFIX + ex.getMessage(), ex);
        });
    }

    /**
     * Sends multiple notifications asynchronously in batch.
     * <p>
     * Each notification is processed independently; one failure does not
     * cancel or affect others (fail-soft). Returns all results once every
     * notification has been processed.
     * </p>
     *
     * @param notifications the notifications to send
     * @return a CompletableFuture that resolves to the list of all results
     */
    public CompletableFuture<List<NotificationResult>> sendBatch(List<? extends Notification> notifications) {
        log.info("Sending batch of {} notifications", notifications.size());

        List<CompletableFuture<NotificationResult>> futures = notifications.stream()
                .map(this::sendAsync)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> {
                    List<NotificationResult> results = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();

                    long successCount = results.stream().filter(NotificationResult::successful).count();
                    long failureCount = results.size() - successCount;
                    log.info("Batch complete: {} sent, {} failed out of {} total",
                            successCount, failureCount, results.size());

                    return results;
                });
    }
}
