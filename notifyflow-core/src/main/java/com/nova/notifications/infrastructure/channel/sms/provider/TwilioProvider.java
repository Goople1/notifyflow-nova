package com.nova.notifications.infrastructure.channel.sms.provider;

import com.nova.notifications.application.port.NotificationProvider;
import com.nova.notifications.domain.exception.ProviderException;
import com.nova.notifications.domain.model.SmsNotification;
import com.nova.notifications.domain.result.NotificationResult;
import com.nova.notifications.common.ProviderConstants;
import com.nova.notifications.common.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Simulated Twilio SMS provider.
 * <p>
 * Simulates the Twilio Messages API ({@code POST /2010-04-01/Accounts/{AccountSid}/Messages.json}).
 * Logs the request structure that would be sent to the real API, including
 * From, To, and Body parameters.
 * </p>
 * <p>
 * Credentials (accountSid, authToken) are never logged or exposed.
 * </p>
 */
public class TwilioProvider implements NotificationProvider<SmsNotification> {

    private static final Logger log = LoggerFactory.getLogger(TwilioProvider.class);

    private final String accountSid;
    private final String authToken;

    /**
     * Creates a new Twilio provider.
     *
     * @param accountSid the Twilio Account SID (never logged)
     * @param authToken  the Twilio Auth Token (never logged)
     */
    public TwilioProvider(String accountSid, String authToken) {
        if (accountSid == null || accountSid.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Twilio.ACCOUNT_SID_ERROR);
        }
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException(ProviderConstants.Twilio.AUTH_TOKEN_ERROR);
        }
        this.accountSid = accountSid;
        this.authToken = authToken;
        log.info("Twilio provider initialized with Account SID [***] and Auth Token [***]");
    }

    @Override
    public NotificationResult send(SmsNotification notification) {
        try {
            log.info("[Twilio] Simulating POST /2010-04-01/Accounts/[***]/Messages.json");
            log.info("[Twilio] Authorization: Basic [***]");
            log.info("[Twilio] Form data: From={}, To={}, Body={}",
                    notification.from(),
                    notification.phoneNumber(),
                    SecurityUtils.truncateForLog(notification.message()));

            String sid = ProviderConstants.Twilio.MESSAGE_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, ProviderConstants.Twilio.MESSAGE_SID_LENGTH);
            log.info("[Twilio] Response: 201 Created, SID: {}", sid);

            return NotificationResult.success(sid);
        } catch (Exception e) {
            log.error("[Twilio] Failed to send SMS to {}: {}", notification.phoneNumber(), e.getMessage());
            throw new ProviderException(ProviderConstants.Twilio.PROVIDER_NAME, "Failed to send SMS: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ProviderConstants.Twilio.PROVIDER_NAME;
    }

}
