package io.freefair.gradle.plugins.lombok;

import io.freefair.gradle.plugins.lombok.tasks.ChangeType;
import io.freefair.gradle.plugins.lombok.tasks.LombokPropertyChange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
@Getter
@Setter
public class LombokExtension {

    /**
     * The version of Lombok which will be used.
     */
    private final Property<String> version;

    /**
     * Imports lombok.config file.
     */
    private final ListProperty<String> imports;

    /**
     * Additional Entries for the lombok.config file.
     */
    private final MapProperty<String, String> config;

    /**
     * Changes made to the lombok.config file.
     */
    private final ListProperty<LombokPropertyChange> configurationChanges;

    @Inject
    public LombokExtension(ObjectFactory objectFactory) {
        version = objectFactory.property(String.class).convention("1.18.12");
        imports = objectFactory.listProperty(String.class);
        config = objectFactory.mapProperty(String.class, String.class);
        configurationChanges = objectFactory.listProperty(LombokPropertyChange.class);
    }

    public LombokPropertyChangeHandler lombokProperty(String propertyName) {
        return new LombokPropertyChangeHandler(propertyName, configurationChanges);
    }

    public void importFile(String fileName) {
        imports.add(fileName);
    }

    @AllArgsConstructor
    public static class LombokPropertyChangeHandler {

        private String propertyName;
        private ListProperty<LombokPropertyChange> configurationChanges;

        public void set(String value) {
            configurationChanges.add(new LombokPropertyChange(propertyName, ChangeType.SET, value));
        }

        public void add(String value) {
            configurationChanges.add(new LombokPropertyChange(propertyName, ChangeType.ADD, value));
        }

        public void remove(String value) {
            configurationChanges.add(new LombokPropertyChange(propertyName, ChangeType.REMOVE, value));
        }

        public void clear() {
            configurationChanges.add(new LombokPropertyChange(propertyName, ChangeType.CLEAR, ""));
        }

        public void addAll(List<String> values) {
            values.forEach(this::add);
        }

        public void removeAll(List<String> values) {
            values.forEach(this::remove);
        }
    }

}
