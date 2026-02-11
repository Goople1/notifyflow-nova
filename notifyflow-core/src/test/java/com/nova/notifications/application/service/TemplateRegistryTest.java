package com.nova.notifications.application.service;

import com.nova.notifications.application.template.TemplateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TemplateRegistry - Message Templates")
class TemplateRegistryTest {

    private TemplateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TemplateRegistry();
    }

    @Test
    @DisplayName("Should render template with variables")
    void renderTemplate() {
        registry.register("welcome", "Hello {{name}}, welcome to {{company}}!");

        var rendered = registry.render("welcome", Map.of("name", "John", "company", "Nova"));

        assertThat(rendered).isEqualTo("Hello John, welcome to Nova!");
    }

    @Test
    @DisplayName("Should throw when rendering missing template")
    void renderMissingTemplate() {
        assertThatThrownBy(() -> registry.render("nonexistent", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    @DisplayName("Should render template with multiple variable replacements")
    void renderMultipleVariables() {
        registry.register("order", "Order #{{orderId}} for {{customer}} - Total: {{total}}");

        var rendered = registry.render("order", Map.of(
                "orderId", "12345",
                "customer", "Alice",
                "total", "$99.99"
        ));

        assertThat(rendered).isEqualTo("Order #12345 for Alice - Total: $99.99");
    }

    @Test
    @DisplayName("Should return Optional.of for existing template")
    void getExistingTemplate() {
        registry.register("test", "content");

        assertThat(registry.get("test")).isPresent();
    }

    @Test
    @DisplayName("Should return Optional.empty for missing template")
    void getMissingTemplate() {
        assertThat(registry.get("missing")).isEmpty();
    }
}
