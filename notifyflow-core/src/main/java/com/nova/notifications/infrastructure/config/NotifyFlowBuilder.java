package com.nova.notifications.infrastructure.config;

import com.nova.notifications.common.ValidationMessages;
import com.nova.notifications.application.async.AsyncNotificationService;
import com.nova.notifications.application.port.NotificationChannel;
import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.application.pubsub.EventListener;
import com.nova.notifications.application.pubsub.EventPublisher;
import com.nova.notifications.application.pubsub.SimpleEventPublisher;
import com.nova.notifications.application.retry.RetryPolicy;
import com.nova.notifications.application.retry.RetryableNotificationService;
import com.nova.notifications.application.service.NotificationService;
import com.nova.notifications.application.template.TemplateRegistry;
import com.nova.notifications.domain.model.*;
import com.nova.notifications.infrastructure.channel.email.EmailChannel;
import com.nova.notifications.infrastructure.channel.email.provider.MailgunProvider;
import com.nova.notifications.infrastructure.channel.email.provider.SendGridProvider;
import com.nova.notifications.infrastructure.channel.email.validation.EmailValidator;
import com.nova.notifications.infrastructure.channel.push.PushChannel;
import com.nova.notifications.infrastructure.channel.push.provider.ApnsProvider;
import com.nova.notifications.infrastructure.channel.push.provider.FcmProvider;
import com.nova.notifications.infrastructure.channel.push.validation.PushValidator;
import com.nova.notifications.infrastructure.channel.slack.SlackChannel;
import com.nova.notifications.infrastructure.channel.slack.provider.SlackWebhookProvider;
import com.nova.notifications.infrastructure.channel.slack.validation.SlackValidator;
import com.nova.notifications.infrastructure.channel.sms.SmsChannel;
import com.nova.notifications.infrastructure.channel.sms.provider.TwilioProvider;
import com.nova.notifications.infrastructure.channel.sms.provider.VonageProvider;
import com.nova.notifications.infrastructure.channel.sms.validation.SmsValidator;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Fluent builder for configuring and assembling the NotifyFlow notification system.
 * <p>
 * This is the main entry point for library consumers. All configuration is done
 * through code using this builder — no YAML, properties, or annotations required.
 * </p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * var notifyFlow = NotifyFlowBuilder.create()
 *     .withEmail(new SendGridProvider("api-key"))
 *     .withSms(new TwilioProvider("sid", "token"))
 *     .withPush(new FcmProvider("server-key"))
 *     .withSlack(new SlackWebhookProvider("webhook-url"))
 *     .withRetryPolicy(RetryPolicy.defaultPolicy())
 *     .onEvent(event -> log.info("Event: {}", event))
 *     .build();
 * }</pre>
 */
public class NotifyFlowBuilder {

    private final Map<ChannelType, NotificationChannel<?>> channels = new EnumMap<>(ChannelType.class);
    private final SimpleEventPublisher eventPublisher = new SimpleEventPublisher();
    private final TemplateRegistry templateRegistry = new TemplateRegistry();

    private RetryPolicy retryPolicy = RetryPolicy.noRetry();
    private Executor asyncExecutor = ForkJoinPool.commonPool();

    private NotifyFlowBuilder() {
    }

    /**
     * Creates a new builder instance.
     */
    public static NotifyFlowBuilder create() {
        return new NotifyFlowBuilder();
    }

    // ========== Email Channel Configuration ==========

    /**
     * Configures the Email channel with a custom provider.
     */
    public NotifyFlowBuilder withEmail(NotificationProvider<EmailNotification> provider) {
        return withEmail(provider, new EmailValidator());
    }

    /**
     * Configures the Email channel with a custom provider and validator.
     */
    public NotifyFlowBuilder withEmail(NotificationProvider<EmailNotification> provider,
                                       NotificationValidator<EmailNotification> validator) {
        channels.put(ChannelType.EMAIL, new EmailChannel(provider, validator));
        return this;
    }

    /**
     * Configures Email with SendGrid provider.
     */
    public NotifyFlowBuilder withSendGrid(String apiKey) {
        return withEmail(new SendGridProvider(apiKey));
    }

    /**
     * Configures Email with Mailgun provider.
     */
    public NotifyFlowBuilder withMailgun(String apiKey, String domain) {
        return withEmail(new MailgunProvider(apiKey, domain));
    }

    // ========== SMS Channel Configuration ==========

    /**
     * Configures the SMS channel with a custom provider.
     */
    public NotifyFlowBuilder withSms(NotificationProvider<SmsNotification> provider) {
        return withSms(provider, new SmsValidator());
    }

    /**
     * Configures the SMS channel with a custom provider and validator.
     */
    public NotifyFlowBuilder withSms(NotificationProvider<SmsNotification> provider,
                                     NotificationValidator<SmsNotification> validator) {
        channels.put(ChannelType.SMS, new SmsChannel(provider, validator));
        return this;
    }

    /**
     * Configures SMS with Twilio provider.
     */
    public NotifyFlowBuilder withTwilio(String accountSid, String authToken) {
        return withSms(new TwilioProvider(accountSid, authToken));
    }

    /**
     * Configures SMS with Vonage provider.
     */
    public NotifyFlowBuilder withVonage(String apiKey, String apiSecret) {
        return withSms(new VonageProvider(apiKey, apiSecret));
    }

    // ========== Push Channel Configuration ==========

    /**
     * Configures the Push channel with a custom provider.
     */
    public NotifyFlowBuilder withPush(NotificationProvider<PushNotification> provider) {
        return withPush(provider, new PushValidator());
    }

    /**
     * Configures the Push channel with a custom provider and validator.
     */
    public NotifyFlowBuilder withPush(NotificationProvider<PushNotification> provider,
                                      NotificationValidator<PushNotification> validator) {
        channels.put(ChannelType.PUSH, new PushChannel(provider, validator));
        return this;
    }

    /**
     * Configures Push with Firebase Cloud Messaging provider.
     */
    public NotifyFlowBuilder withFcm(String serverKey) {
        return withPush(new FcmProvider(serverKey));
    }

    /**
     * Configures Push with Apple Push Notification service provider.
     */
    public NotifyFlowBuilder withApns(String teamId, String keyId, String bundleId) {
        return withPush(new ApnsProvider(teamId, keyId, bundleId));
    }

    // ========== Slack Channel Configuration ==========

    /**
     * Configures the Slack channel with a custom provider.
     */
    public NotifyFlowBuilder withSlack(NotificationProvider<SlackNotification> provider) {
        return withSlack(provider, new SlackValidator());
    }

    /**
     * Configures the Slack channel with a custom provider and validator.
     */
    public NotifyFlowBuilder withSlack(NotificationProvider<SlackNotification> provider,
                                       NotificationValidator<SlackNotification> validator) {
        channels.put(ChannelType.SLACK, new SlackChannel(provider, validator));
        return this;
    }

    /**
     * Configures Slack with Incoming Webhook provider.
     */
    public NotifyFlowBuilder withSlackWebhook(String webhookUrl) {
        return withSlack(new SlackWebhookProvider(webhookUrl));
    }

    // ========== Generic Channel Registration ==========

    /**
     * Registers a custom channel for any ChannelType.
     * <p>
     * This enables extensibility: external consumers can add channels
     * for new ChannelTypes without modifying the library.
     * </p>
     */
    public NotifyFlowBuilder withChannel(ChannelType type, NotificationChannel<?> channel) {
        channels.put(type, channel);
        return this;
    }

    // ========== Cross-cutting Configuration ==========

    /**
     * Configures the retry policy for failed sends.
     */
    public NotifyFlowBuilder withRetryPolicy(RetryPolicy policy) {
        this.retryPolicy = policy;
        return this;
    }

    /**
     * Registers an event listener for notification lifecycle events.
     */
    public NotifyFlowBuilder onEvent(EventListener listener) {
        this.eventPublisher.subscribe(listener);
        return this;
    }

    /**
     * Configures the executor for async operations.
     */
    public NotifyFlowBuilder withAsyncExecutor(Executor executor) {
        this.asyncExecutor = executor;
        return this;
    }

    /**
     * Registers a message template.
     */
    public NotifyFlowBuilder withTemplate(String name, String templateContent) {
        this.templateRegistry.register(name, templateContent);
        return this;
    }

    // ========== Build ==========

    /**
     * Builds and returns the configured NotifyFlow instance.
     *
     * @return the fully configured NotifyFlow facade
     * @throws IllegalStateException if no channels are configured
     */
    public NotifyFlow build() {
        if (channels.isEmpty()) {
            throw new IllegalStateException(ValidationMessages.AT_LEAST_ONE_CHANNEL);
        }

        var notificationService = new NotificationService(channels, eventPublisher);
        var retryService = new RetryableNotificationService(notificationService, retryPolicy, eventPublisher);
        var asyncService = new AsyncNotificationService(notificationService, asyncExecutor);

        return new NotifyFlow(notificationService, retryService, asyncService, templateRegistry, eventPublisher);
    }
}
