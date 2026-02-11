package com.nova.notifications.domain.model;

import java.util.List;

/**
 * Notification for the Email channel.
 * <p>
 * Models the data required by email providers like SendGrid and Mailgun:
 * sender address, recipient(s), subject, HTML/text body, CC and BCC lists.
 * </p>
 *
 * @param from    sender email address
 * @param to      primary recipient email address
 * @param subject email subject line
 * @param body    email body content (HTML or plain text)
 * @param isHtml  whether the body is HTML content
 * @param cc      list of CC recipients (may be empty)
 * @param bcc     list of BCC recipients (may be empty)
 */
public record EmailNotification(
        String from,
        String to,
        String subject,
        String body,
        boolean isHtml,
        List<String> cc,
        List<String> bcc
) implements Notification {

    public EmailNotification {
        cc = cc != null ? List.copyOf(cc) : List.of();
        bcc = bcc != null ? List.copyOf(bcc) : List.of();
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public String recipient() {
        return to;
    }

    /**
     * Convenience factory for simple text emails.
     */
    public static EmailNotification simple(String from, String to, String subject, String body) {
        return new EmailNotification(from, to, subject, body, false, List.of(), List.of());
    }

    /**
     * Convenience factory for HTML emails.
     */
    public static EmailNotification html(String from, String to, String subject, String htmlBody) {
        return new EmailNotification(from, to, subject, htmlBody, true, List.of(), List.of());
    }
}
