package com.nova.notifications.domain.model;

/**
 * Notification for the Slack channel (optional).
 * <p>
 * Models the data required by Slack Incoming Webhooks API:
 * channel/webhook URL, message text, optional username and icon override.
 * </p>
 *
 * @param channel   Slack channel name or webhook identifier
 * @param message   message text (supports Slack markdown/mrkdwn)
 * @param username  optional bot username override
 * @param iconEmoji optional icon emoji for the message (e.g., ":rocket:")
 */
public record SlackNotification(
        String channel,
        String message,
        String username,
        String iconEmoji
) implements Notification {

    @Override
    public ChannelType channelType() {
        return ChannelType.SLACK;
    }

    @Override
    public String recipient() {
        return channel;
    }

    /**
     * Convenience factory for simple Slack messages.
     */
    public static SlackNotification simple(String channel, String message) {
        return new SlackNotification(channel, message, null, null);
    }
}
