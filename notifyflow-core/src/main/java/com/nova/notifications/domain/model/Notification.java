package com.nova.notifications.domain.model;

/**
 * Sealed base interface for all notification types.
 * <p>
 * Uses Java 21 sealed interface to define a closed set of notification types,
 * enabling exhaustive pattern matching in switch expressions. Each permitted
 * type is an immutable record representing a specific channel's data model.
 * </p>
 * <p>
 * Extensibility note: While the notification types are sealed (finite channels),
 * the providers for each channel are open for extension via the Strategy pattern.
 * New providers (e.g., Amazon SES for email) can be added without modifying
 * any existing code. To add a new channel, add a new permitted record here
 * and implement the corresponding channel, provider, and validator.
 * </p>
 */
public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification, SlackNotification {

    /**
     * @return the channel type this notification belongs to
     */
    ChannelType channelType();

    /**
     * @return the primary recipient identifier (email, phone, token, etc.)
     */
    String recipient();
}
