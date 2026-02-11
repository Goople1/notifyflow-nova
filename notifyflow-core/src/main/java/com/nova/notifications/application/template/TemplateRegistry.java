package com.nova.notifications.application.template;

import com.nova.notifications.common.ValidationMessages;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for notification templates.
 * <p>
 * Thread-safe template storage that allows registering and
 * retrieving templates by name. Templates are immutable records.
 * </p>
 */
public class TemplateRegistry {

    private final Map<String, NotificationTemplate> templates = new ConcurrentHashMap<>();

    /**
     * Registers a template. Overwrites if name already exists.
     */
    public TemplateRegistry register(String name, String templateContent) {
        templates.put(name, new NotificationTemplate(name, templateContent));
        return this;
    }

    /**
     * Retrieves a template by name.
     */
    public Optional<NotificationTemplate> get(String name) {
        return Optional.ofNullable(templates.get(name));
    }

    /**
     * Renders a template by name with the given variables.
     *
     * @throws IllegalArgumentException if template not found
     */
    public String render(String templateName, Map<String, String> variables) {
        return get(templateName)
                .orElseThrow(() -> new IllegalArgumentException(ValidationMessages.TEMPLATE_NOT_FOUND + templateName))
                .render(variables);
    }
}
