package com.nova.notifications.demo;

import com.nova.notifications.application.retry.RetryPolicy;
import com.nova.notifications.domain.event.NotificationEvent;
import com.nova.notifications.domain.model.*;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.infrastructure.config.NotifyFlow;
import com.nova.notifications.infrastructure.config.NotifyFlowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Demonstration of NotifyFlow library capabilities.
 * <p>
 * Showcases: sync/async/batch sending, retry, templates, pub/sub events,
 * and multi-channel support (Email, SMS, Push, Slack).
 * </p>
 */
public class NotifyFlowDemo {

    private static final Logger log = LoggerFactory.getLogger(NotifyFlowDemo.class);

    public static void main(String[] args) {
        var mode = args.length > 0 ? args[0] : "all";

        log.info("=== NotifyFlow Nova Demo ===");
        log.info("Mode: {}", mode);

        var notifyFlow = buildNotifyFlow();

        switch (mode) {
            case "sync" -> runSyncDemo(notifyFlow);
            case "async" -> runAsyncDemo(notifyFlow);
            case "batch" -> runBatchDemo(notifyFlow);
            case "retry" -> runRetryDemo();
            case "template" -> runTemplateDemo(notifyFlow);
            case "validation" -> runValidationDemo(notifyFlow);
            case "all" -> {
                runSyncDemo(notifyFlow);
                runAsyncDemo(notifyFlow);
                runBatchDemo(notifyFlow);
                runRetryDemo();
                runTemplateDemo(notifyFlow);
                runValidationDemo(notifyFlow);
            }
            default -> log.warn("Unknown mode: {}. Use: sync, async, batch, retry, template, validation, all", mode);
        }

        log.info("=== Demo Complete ===");
    }

    private static NotifyFlow buildNotifyFlow() {
        return NotifyFlowBuilder.create()
                .withSendGrid("SG.demo-api-key-placeholder")
                .withTwilio("AC-demo-account-sid", "demo-auth-token")
                .withFcm("demo-fcm-server-key")
                .withSlackWebhook("https://hooks.slack.com/services/T00/B00/demo")
                .withTemplate("welcome", "Hello {{name}}, welcome to {{company}}!")
                .withTemplate("otp", "Your verification code is: {{code}}. Expires in {{minutes}} minutes.")
                .withTemplate("order", "Order #{{orderId}} confirmed. Total: {{total}}")
                .onEvent(NotifyFlowDemo::logEvent)
                .build();
    }

    private static void runSyncDemo(NotifyFlow notifyFlow) {
        log.info("\n--- Synchronous Send Demo ---");

        // Email
        var email = EmailNotification.html(
                "noreply@company.com",
                "user@client.com",
                "Welcome to NotifyFlow",
                "<h1>Welcome!</h1><p>Your account has been created.</p>"
        );
        printResult("Email", notifyFlow.send(email));

        // SMS
        var sms = new SmsNotification("+15551000000", "+15559876543", "Your code is 123456");
        printResult("SMS", notifyFlow.send(sms));

        // Push
        var push = PushNotification.withData(
                "dGhpcyBpcyBhIGRldmljZSB0b2tlbg",
                "New Message",
                "You have a new message from Alice",
                Map.of("action", "open_chat", "chatId", "chat-42")
        );
        printResult("Push", notifyFlow.send(push));

        // Slack
        var slack = new SlackNotification("#deployments", "Deployment v2.1.0 completed successfully", "DeployBot", ":rocket:");
        printResult("Slack", notifyFlow.send(slack));
    }

    private static void runAsyncDemo(NotifyFlow notifyFlow) {
        log.info("\n--- Asynchronous Send Demo ---");

        var email = EmailNotification.simple("noreply@app.com", "dev@team.com", "Async Test", "This was sent async!");

        notifyFlow.sendAsync(email)
                .thenAccept(result -> printResult("Async Email", result))
                .join();

        var sms = new SmsNotification("+15551000000", "+15551112222", "Async SMS test");
        notifyFlow.sendAsync(sms)
                .thenAccept(result -> printResult("Async SMS", result))
                .join();
    }

    private static void runBatchDemo(NotifyFlow notifyFlow) {
        log.info("\n--- Batch Send Demo ---");

        List<Notification> notifications = List.of(
                EmailNotification.simple("noreply@app.com", "user1@test.com", "Batch 1", "First batch email"),
                EmailNotification.simple("noreply@app.com", "user2@test.com", "Batch 2", "Second batch email"),
                new SmsNotification("+15551000000", "+15553334444", "Batch SMS"),
                PushNotification.simple("batch-device-token-1234567890", "Batch Alert", "Batch push notification"),
                SlackNotification.simple("#alerts", "Batch processing complete")
        );

        var results = notifyFlow.sendBatch(notifications).join();

        long success = results.stream().filter(NotificationResult::successful).count();
        long failed = results.size() - success;
        log.info("Batch results: {} success, {} failed out of {} total", success, failed, results.size());
    }

    private static void runRetryDemo() {
        log.info("\n--- Retry Demo ---");

        var notifyFlowWithRetry = NotifyFlowBuilder.create()
                .withSendGrid("SG.retry-demo-key")
                .withRetryPolicy(RetryPolicy.defaultPolicy())
                .onEvent(NotifyFlowDemo::logEvent)
                .build();

        var email = EmailNotification.simple("noreply@app.com", "retry@test.com", "Retry Test", "Testing retries");
        var result = notifyFlowWithRetry.sendWithRetry(email);
        printResult("Retry Email", result);
    }

    private static void runTemplateDemo(NotifyFlow notifyFlow) {
        log.info("\n--- Template Demo ---");

        var welcomeBody = notifyFlow.renderTemplate("welcome", Map.of("name", "Alice", "company", "TechCorp"));
        log.info("Rendered welcome: {}", welcomeBody);

        var otpBody = notifyFlow.renderTemplate("otp", Map.of("code", "847291", "minutes", "5"));
        log.info("Rendered OTP: {}", otpBody);

        // Use rendered template in a notification
        var email = EmailNotification.simple("noreply@app.com", "alice@client.com", "Welcome!", welcomeBody);
        printResult("Template Email", notifyFlow.send(email));

        var sms = new SmsNotification("+15551000000", "+15559876543", otpBody);
        printResult("Template SMS", notifyFlow.send(sms));
    }

    private static void runValidationDemo(NotifyFlow notifyFlow) {
        log.info("\n--- Validation Error Demo ---");

        // Invalid email
        var badEmail = EmailNotification.simple("", "not-an-email", "", "");
        printResult("Invalid Email", notifyFlow.send(badEmail));

        // Invalid SMS
        var badSms = new SmsNotification("+1", "123", "");
        printResult("Invalid SMS", notifyFlow.send(badSms));

        // Invalid Push
        var badPush = PushNotification.simple("short", "", null);
        printResult("Invalid Push", notifyFlow.send(badPush));

        // Null notification
        printResult("Null", notifyFlow.send(null));
    }

    private static void printResult(String label, NotificationResult result) {
        if (result.successful()) {
            log.info("[{}] SUCCESS - ID: {}", label, result.notificationId());
        } else {
            log.info("[{}] FAILED - Source: {}, Message: {}", label, result.errorSource(), result.errorMessage());
        }
    }

    private static void logEvent(NotificationEvent event) {
        log.debug("  EVENT: {} | {} | {} | attempt={}",
                event.eventType(), event.channelType(), event.recipient(), event.attempt());
    }
}
