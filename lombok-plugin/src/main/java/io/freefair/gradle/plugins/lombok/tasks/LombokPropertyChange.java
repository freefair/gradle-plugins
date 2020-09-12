package io.freefair.gradle.plugins.lombok.tasks;

import lombok.AllArgsConstructor;
import org.gradle.api.tasks.Input;

/**
 * Represents a property change in the lombok.config file
 */
@AllArgsConstructor
public class LombokPropertyChange {

    private String propertyName;
    private ChangeType changeType;
    private String value;

    @Input
    public String getPropertyName() {
        return propertyName;
    }

    @Input
    public ChangeType getChangeType() {
        return changeType;
    }

    @Input
    public String getValue() {
        return value;
    }

}
