package io.freefair.gradle.plugins.war;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.copy.CopySpecInternal;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.bundling.War;

import java.util.Set;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@Getter
@Setter
public class WarOverlay {

    private final String name;
    private final War warTask;
    private CopySpecInternal warCopySpec;

    /**
     * The source of the overlay, this can be:
     * <ul>
     * <li>An arbitrary {@link org.gradle.api.tasks.bundling.AbstractArchiveTask AbstractArchiveTask}</li>
     * <li>An other Project which has the {@link org.gradle.api.plugins.WarPlugin 'war'-plugin} applied</li>
     * <li>Anything that can be used as Dependency. See {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}</li>
     * </ul>
     */
    private Object source;

    /**
     * This {@link Closure} is passed to {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object, Closure)}
     * when {@link #source} is resolved as dependency.
     *
     * @see org.gradle.api.artifacts.dsl.DependencyHandler#create(Object, Closure)
     */
    private Closure configureClosure;

    private boolean enabled = true;

    private boolean provided = false;

    /**
     * If set to true (default) the {@link #provided} setting is applied to the {@link org.gradle.api.tasks.bundling.War War task}
     * before its execution.
     * If set to false, the task will be completely configured at configuration time.
     * <p>
     * This can be used to get auto-competition in some IDE's (IntelliJ)
     */
    private boolean deferProvidedConfiguration = true;

    /**
     * Enable (java-)compilation against the classes({@code WEB-INF/classes}) and jars({@code WEB-INF/lib}) of the overlay
     */
    private boolean enableCompilation = true;

    public WarOverlay(String name, War warTask) {
        this.name = name;
        this.warTask = warTask;
        this.warCopySpec = warTask.getRootSpec().addChild();

        warCopySpec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
        exclude("META-INF/maven/**");
        exclude("META-INF/MANIFEST.MF");
    }

    public void from(Object object) {
        source = object;
        this.configureClosure = null;
    }

    /**
     * @param object           see {@link #source}
     * @param configureClosure see {@link #configureClosure}
     */
    public void from(Object object, Closure configureClosure) {
        source = object;
        this.configureClosure = configureClosure;
    }

    public void provided() {
        provided(true);
    }

    public void provided(boolean provided) {
        setProvided(provided);
    }

    /**
     * @see #into
     */
    public void setTargetPath(String targetPath) {
        setInto(targetPath);
    }

    /**
     * @see #enabled
     */
    public void setSkip(boolean skip) {
        setEnabled(!skip);
    }

    /**
     * @see #enabled
     */
    public boolean isSkip() {
        return !isEnabled();
    }

    public String getConfigurationName() {
        return String.format("%s%sOverlay", getName(), capitalize((CharSequence) getWarTask().getName()));
    }

    public void setInto(String destPath) {
        into(destPath);
    }

    public void into(Object destPath) {
        getWarCopySpec().into(destPath);
    }

    public void setExcludes(Iterable<String> excludes) {
        getWarCopySpec().setExcludes(excludes);
    }

    public void exclude(String... excludes) {
        getWarCopySpec().exclude(excludes);
    }

    public void exclude(Iterable<String> excludes) {
        getWarCopySpec().exclude(excludes);
    }

    public void exclude(Spec<FileTreeElement> excludeSpec) {
        getWarCopySpec().exclude(excludeSpec);
    }

    public void exclude(Closure excludeSpec) {
        getWarCopySpec().exclude(excludeSpec);
    }

    public Set<String> getExcludes() {
        return getWarCopySpec().getExcludes();
    }
}
