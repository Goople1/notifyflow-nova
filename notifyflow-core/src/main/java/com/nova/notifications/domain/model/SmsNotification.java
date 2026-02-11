package com.nova.notifications.domain.model;

/**
 * Notification for the SMS channel.
 * <p>
 * Models the data required by SMS providers like Twilio and Vonage:
 * sender number, recipient phone number, and text message body.
 * Phone numbers should follow E.164 international format (+1234567890).
 * </p>
 *
 * @param from        sender phone number or alphanumeric sender ID
 * @param phoneNumber recipient phone number in E.164 format
 * @param message     SMS text body (plain text, max ~160 chars for single SMS)
 */
public record SmsNotification(
        String from,
        String phoneNumber,
        String message
) implements Notification {

    @Override
    public ChannelType channelType() {
        return ChannelType.SMS;
    }

    @Override
    public String recipient() {
        return phoneNumber;
    }
}
