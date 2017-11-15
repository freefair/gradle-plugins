package io.freefair.gradle.plugins.war;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class WarOverlay {

    private final String name;

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

    /**
     * @see org.gradle.api.file.CopySpec#into(Object)
     */
    private String into = "";
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
     * @see org.gradle.api.file.CopySpec#exclude(Iterable)
     */
    private Set<String> excludes = new HashSet<>();

    /**
     * Enable (java-)compilation against the classes({@code WEB-INF/classes}) and jars({@code WEB-INF/lib}) of the overlay
     */
    private boolean enableCompilation = true;

    public WarOverlay(String name) {
        this.name = name;
        excludes.add("META-INF/maven/**");
        excludes.add("META-INF/MANIFEST.MF");
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

    /**
     * @see org.gradle.api.file.CopySpec#exclude(String...)
     */
    public void exclude(String... pattern) {
        Collections.addAll(excludes, pattern);
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
     * @see #into
     */
    public String getTargetPath() {
        return getInto();
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
}
