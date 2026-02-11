package com.nova.notifications.infrastructure.channel.sms;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.application.port.NotificationValidator;
import com.nova.notifications.domain.model.ChannelType;
import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.infrastructure.channel.AbstractNotificationChannel;

/**
 * SMS notification channel.
 * Inherits the Template Method flow from {@link AbstractNotificationChannel}.
 */
public class SmsChannel extends AbstractNotificationChannel<SmsNotification> {

    public SmsChannel(NotificationProvider<SmsNotification> provider,
                      NotificationValidator<SmsNotification> validator) {
        super(provider, validator);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }
}
