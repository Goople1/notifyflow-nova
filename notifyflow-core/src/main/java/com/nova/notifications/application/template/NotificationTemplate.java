package com.nova.notifications.application.template;

import com.nova.notifications.common.TemplateConstants;

import java.util.Map;

/**
 * Reusable notification template with variable placeholders.
 * <p>
 * Allows defining message templates with {{variable}} placeholders
 * that are resolved at send time. Useful for transactional emails,
 * welcome messages, password resets, etc.
 * </p>
 *
 * @param name     unique template name for identification
 * @param template the template string with {{variable}} placeholders
 */
public record NotificationTemplate(String name, String template) {

    /**
     * Renders the template by replacing all {{key}} placeholders
     * with the corresponding values from the provided map.
     *
     * @param variables key-value pairs for placeholder replacement
     * @return the rendered string with all placeholders replaced
     */
    public String render(Map<String, String> variables) {
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace(TemplateConstants.PLACEHOLDER_PREFIX + entry.getKey() + TemplateConstants.PLACEHOLDER_SUFFIX, entry.getValue());
        }
        return rendered;
    }
}
