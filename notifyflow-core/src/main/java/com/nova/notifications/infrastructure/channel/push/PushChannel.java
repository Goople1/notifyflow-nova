package com.nova.notifications.infrastructure.channel.push;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.PushNotification;
import com.nova.notifications.infrastructure.channel.AbstractNotificationChannel;

/**
 * Push notification channel.
 * Inherits the Template Method flow from {@link AbstractNotificationChannel}.
 */
public class PushChannel extends AbstractNotificationChannel<PushNotification> {

    public PushChannel(NotificationProvider<PushNotification> provider,
                       NotificationValidator<PushNotification> validator) {
        super(provider, validator);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }
}
