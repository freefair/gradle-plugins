package io.freefair.gradle.plugins.aspectj.internal;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Buildable;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.FailingFileCollection;
import org.gradle.api.internal.file.collections.LazilyInitializedFileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see org.gradle.api.tasks.GroovyRuntime
 */
@Slf4j
public class AspectJRuntime {

    private final ProjectInternal project;

    public AspectJRuntime(Project project) {
        this.project = (ProjectInternal) project;
    }

    /**
     * Searches the specified class path for AspectJ Jars ({@code aspectjrt}, {@code aspectjweaver}) and returns a corresponding class path for executing AspectJ tools such as the ajc
     * compiler. The tool versions will match those of the AspectJ Jars found.
     *
     * <p>The returned class path may be empty, or may fail to resolve when asked for its contents.
     *
     * @param classpath a class path containing AspectJ Jars
     * @return a corresponding class path for executing AspectJ tools such as the AspectJ compiler
     * @see org.gradle.api.tasks.GroovyRuntime#inferGroovyClasspath(Iterable)
     */
    public FileCollection inferAspectjClasspath(final FileCollection classpath) {
        // alternatively, we could return project.getLayout().files(Runnable)
        // would differ in at least the following ways: 1. live 2. no autowiring
        return new LazilyInitializedFileCollection() {

            @Override
            public String getDisplayName() {
                return "AspectJ runtime classpath";
            }

            @Override
            public FileCollection createDelegate() {
                try {
                    return inferAspectjClasspath();
                } catch (RuntimeException e) {
                    return new FailingFileCollection(getDisplayName(), e);
                }
            }

            private FileCollection inferAspectjClasspath() {

                File aspectjtoolsJarFile = findAspectjtoolsJarFile(classpath);

                if (aspectjtoolsJarFile != null) {
                    log.warn("Found tools jar: {}", aspectjtoolsJarFile);
                    return project.getLayout().files(aspectjtoolsJarFile);
                }

                String versionNumber = findAspectjVersion(classpath);

                if (versionNumber == null) {
                    throw new GradleException(
                            String.format(
                                    "Cannot infer AspectJ class path because no AspectJ Jar was found on class path: %s",
                                    classpath
                            )
                    );
                }

                String notation = "org.aspectj:aspectjtools:" + versionNumber;

                return detachedRuntimeClasspath(project.getDependencies().create(notation));
            }

            private Configuration detachedRuntimeClasspath(Dependency... dependencies) {
                Configuration classpath = project.getConfigurations().detachedConfiguration(dependencies);
                jvmEcosystemUtilities().configureAsRuntimeClasspath(classpath);
                return classpath;
            }

            // let's override this so that delegate isn't created at autowiring time (which would mean on every build)
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                if (classpath != null) {
                    context.add(classpath);
                }
            }
        };
    }

    @Nullable
    private static File findAspectjtoolsJarFile(FileCollection classpath) {
        if (classpath == null) {
            return null;
        }
        for (File file : classpath) {
            if (file.getName().startsWith("aspectjtools") && file.getName().endsWith(".jar")) {
                return file;
            }
        }
        return null;
    }

    private static final Pattern aspectjVersionPattern = Pattern.compile("aspectj\\w+-(\\d.*).jar");

    private static String findAspectjVersion(FileCollection classpath) {
        if (classpath == null) {
            return null;
        }
        for (File file : classpath) {
            Matcher matcher = aspectjVersionPattern.matcher(file.getName());
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        for (File file : classpath) {
            String fileName = file.getName();
            if (fileName.startsWith("aspectj") && fileName.endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(file);
                    Manifest manifest = jarFile.getManifest();
                    if (manifest != null) {
                        Object version = manifest.getMainAttributes().get("Implementation-Version");
                        if (version != null) {
                            return version.toString();
                        }
                    }
                } catch (IOException ignored) {

                }
            }
        }

        return null;
    }

    private JvmEcosystemUtilities jvmEcosystemUtilities() {
        return project.getServices().get(JvmEcosystemUtilities.class);
    }
}
