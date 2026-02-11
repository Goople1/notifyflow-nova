package com.nova.notifications.domain.exception;

import com.nova.notifications.common.ValidationMessages;

import java.util.List;

/**
 * Exception thrown when notification validation fails.
 * <p>
 * Contains a list of all validation errors found, enabling
 * the caller to display all issues at once rather than one at a time.
 * </p>
 */
public final class ValidationException extends NotificationException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(ValidationMessages.VALIDATION_FAILED_PREFIX + String.join(ValidationMessages.ERROR_SEPARATOR, errors));
        this.errors = List.copyOf(errors);
    }

    public ValidationException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }
}
