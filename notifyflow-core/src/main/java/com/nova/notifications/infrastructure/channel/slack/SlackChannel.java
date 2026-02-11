package com.nova.notifications.infrastructure.channel.slack;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.SlackNotification;
import com.nova.notifications.infrastructure.channel.AbstractNotificationChannel;

/**
 * Slack notification channel.
 * Inherits the Template Method flow from {@link AbstractNotificationChannel}.
 */
public class SlackChannel extends AbstractNotificationChannel<SlackNotification> {

    public SlackChannel(NotificationProvider<SlackNotification> provider,
                        NotificationValidator<SlackNotification> validator) {
        super(provider, validator);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SLACK;
    }
}
