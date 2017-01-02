package io.freefair.gradle.plugins;

import lombok.Getter;
import org.gradle.api.Project;

import java.beans.Introspector;

/**
 * @param <E> the type of the extension
 * @author Lars Grefer
 */
@Getter
public abstract class AbstractExtensionPlugin<E> extends AbstractPlugin {

    protected E extension;

    @Override
    public void apply(Project project) {
        super.apply(project);

        createExtension();
    }

    protected void createExtension() {
        extension = project.getExtensions().create(getExtensionName(), getExtensionClass());
    }

    protected String getExtensionName() {
        String extensionName = Introspector.decapitalize(getExtensionClass().getSimpleName());

        if(extensionName.endsWith("Extension")) {
            extensionName = extensionName.replace("Extension", "");
        }

        return extensionName;
    }

    protected abstract Class<E> getExtensionClass();
}
