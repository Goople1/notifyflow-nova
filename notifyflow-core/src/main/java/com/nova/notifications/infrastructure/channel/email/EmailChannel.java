package com.nova.notifications.infrastructure.channel.email;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.EmailNotification;
import com.nova.notifications.infrastructure.channel.AbstractNotificationChannel;

/**
 * Email notification channel.
 * Inherits the Template Method flow from {@link AbstractNotificationChannel}.
 */
public class EmailChannel extends AbstractNotificationChannel<EmailNotification> {

    public EmailChannel(NotificationProvider<EmailNotification> provider,
                        NotificationValidator<EmailNotification> validator) {
        super(provider, validator);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }
}
